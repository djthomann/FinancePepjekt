package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.ROUNDING_NUMBER_TO_DECIMAL_PLACE
import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.entities.Owner
import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import de.hsrm.mi.stacs.pepjekt.entities.dtos.*
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.*
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for managing investment accounts, including buying and selling stocks,
 * as well as retrieving account portfolios.
 *
 * This service interacts with investment account and stock repositories, ensuring
 * transactional integrity for financial operations.
 */
@Service
class InvestmentAccountService(
    val operator: TransactionalOperator, // injected by spring
    val investmentAccountRepository: IInvestmentAccountRepository,
    val portfolioEntryRepository: IPortfolioEntryRepository,
    val bankAccountRepository: IBankAccountRepository,
    val ownerRepository: IOwnerRepository,
    val stockRepository: IStockRepository,
    val finnhubHandler: FinnhubHandler,
    val latestIStockQuoteRepository: IStockQuoteLatestRepository,
    val quoteRepository: IStockQuoteRepository
) : IInvestmentAccountService {

    /**
     * Buys a stock and updates the investment account portfolio and linked bank account.
     *
     * This method processes a stock purchase by validating input, calculating the stock volume to purchase,
     * updating the portfolio entry, and withdrawing the purchase amount from the linked bank account.
     *
     * **Key Calculations:**
     * - calculatedVolume = purchaseAmount / currentStockPrice
     *   --> Calculates the number of stocks to purchase based on the given amount and current stock price.
     * - updatedQuantity = existingQuantity + calculatedVolume
     *   --> Updates the stock quantity for an existing portfolio entry.
     * - updatedBalance = bankAccountBalance - purchaseAmount
     *   --> withdraws the purchase amount from the bank account balance.
     *
     * **Key Scenarios:**
     * - If the portfolio entry for the stock exists, it is updated with the new quantity and investment amount.
     * - If the portfolio entry does not exist, a new entry is created.
     *
     * @param investmentAccountId the ID of the investment account making the purchase
     * @param stockSymbol the symbol of the stock to purchase
     * @param purchaseAmount the amount of money to spend on purchasing the stock
     * @return the updated InvestmentAccount or an error if any operation fails
     */
    override fun buyStock(
        investmentAccountId: Long, stockSymbol: String, purchaseAmount: BigDecimal
    ): Mono<InvestmentAccount> {
        return investmentAccountRepository.findById(investmentAccountId).flatMap { investmentAccount ->
            if (investmentAccount.bankAccountId == null) {
                return@flatMap Mono.error<InvestmentAccount>(IllegalArgumentException("No bank account linked to the investment account"))
            }

            latestIStockQuoteRepository.findById(stockSymbol).flatMap { latestStockQuote ->
                quoteRepository.findById(latestStockQuote.quoteId)
            }.flatMap { quote ->
                val calculatedVolume = purchaseAmount / quote.currentPrice

                portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(
                    investmentAccountId, stockSymbol
                ).flatMap { existingEntry ->
                    // Update the existing entry
                    val updatedQuantity = existingEntry.quantity + calculatedVolume.toDouble()
                    val updatedEntry = existingEntry.copy(
                        quantity = updatedQuantity,
                        totalInvestAmount = existingEntry.totalInvestAmount.plus(purchaseAmount)
                    )
                    portfolioEntryRepository.save(updatedEntry).`as`(operator::transactional)
                }.switchIfEmpty(
                    // Create a new entry if none exists
                    portfolioEntryRepository.save(
                        PortfolioEntry(
                            investmentAccountId = investmentAccountId,
                            stockSymbol = stockSymbol,
                            quantity = calculatedVolume.toDouble(),
                            totalInvestAmount = purchaseAmount,
                        )
                    ).`as`(operator::transactional)
                ).flatMap {
                    // Update the balance in the linked bank account
                    bankAccountRepository.findById(investmentAccount.bankAccountId)
                }.flatMap { bankAccount ->
                    val updatedBalance = bankAccount.balance.minus(purchaseAmount)
                    if (updatedBalance < BigDecimal.ZERO) {
                        return@flatMap Mono.error<InvestmentAccount>(
                            IllegalArgumentException(
                                "Investmentaccount " + investmentAccountId + ": Insufficient balance in the bank account buying " + stockSymbol + " for amount " + purchaseAmount
                            )
                        )
                    }

                    val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                    bankAccountRepository.save(updatedBankAccount).`as`(operator::transactional)
                }.thenReturn(investmentAccount)
            }
        }
    }


    /**
     * Sells a specified volume of stock from an investment account.
     *
     * This method handles the process of selling a stock from a user's portfolio. It validates the input,
     * ensures the investment account and stock exist, and performs the necessary calculations to update
     * the portfolio and linked bank account.
     *
     * **Calculations:**
     * - investReduction = totalInvestAmount * (soldVolume / existingQuantity)
     *   --> Reduces the total investment amount proportionally to the volume of stocks sold.
     * - newTotalInvestAmount = totalInvestAmount - investReduction
     *   --> Updates the total investment amount after selling.
     * - updatedBalance = bankAccountBalance + (soldVolume * stockPrice)
     *   --> Adds the value of the sold stocks to the linked bank account balance.
     *
     * **Key Scenarios:**
     * - If the new stock quantity is greater than 0, the portfolio entry is updated.
     * - If the new stock quantity is 0, the portfolio entry is deleted.
     *
     * @param investmentAccountId the ID of the investment account
     * @param stockSymbol the symbol of the stock to sell
     * @param volume the quantity of stock to sell
     * @return Updated InvestmentAccount
     */
    override fun sellStock(
        investmentAccountId: Long, stockSymbol: String, volume: Double
    ): Mono<InvestmentAccount> {
        return investmentAccountRepository.findById(investmentAccountId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Investment account not found")))
            .flatMap { investmentAccount ->
                if (investmentAccount.bankAccountId == null) {
                    return@flatMap Mono.error<InvestmentAccount>(IllegalArgumentException("No bank account linked to the investment account"))
                }

                portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
                    .switchIfEmpty(Mono.error(IllegalArgumentException("Stock not found in portfolio")))
                    .flatMap { existingEntry ->
                        val newQuantity = existingEntry.quantity - volume
                        if (newQuantity < 0) {
                            return@flatMap Mono.error<InvestmentAccount>(IllegalArgumentException("Insufficient stock quantity to sell"))
                        }

                        latestIStockQuoteRepository.findById(stockSymbol)
                            .switchIfEmpty(Mono.error(IllegalArgumentException("Stock quote not found")))
                            .flatMap { latestStockQuote ->
                                quoteRepository.findById(latestStockQuote.quoteId)
                            }.switchIfEmpty(Mono.error(IllegalArgumentException("Quote not found"))).flatMap {
                                //investReduction = totalInvestAmount * (sold amount of volume / old amount of volume)
                                val investReduction = existingEntry.totalInvestAmount.multiply(
                                    volume.toBigDecimal().divide(
                                        existingEntry.quantity.toBigDecimal(),
                                        ROUNDING_NUMBER_TO_DECIMAL_PLACE,
                                        RoundingMode.HALF_UP
                                    )
                                )
                                val newTotalInvestAmount = existingEntry.totalInvestAmount.subtract(investReduction)

                                if (newQuantity > 0) {
                                    val updatedEntry = existingEntry.copy(
                                        quantity = newQuantity, totalInvestAmount = newTotalInvestAmount
                                    )
                                    portfolioEntryRepository.save(updatedEntry)
                                } else {
                                    portfolioEntryRepository.delete(existingEntry).then(Mono.empty())
                                }
                            }.then(bankAccountRepository.findById(investmentAccount.bankAccountId))
                            .flatMap { bankAccount ->
                                val updatedBalance = bankAccount.balance.add(volume.toBigDecimal())
                                val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                                bankAccountRepository.save(updatedBankAccount)
                            }.thenReturn(investmentAccount)
                    }
            }
    }

    /**
     * Retrieves the portfolio of an investment account by the investmentAccountId ID.
     *
     * @param investmentAccountId the ID of the investmentAccount whose investment account portfolio is to be retrieved
     * @return a [Mono] emitting the [InvestmentAccount] containing the portfolio, or an error if not found
     */
    override fun getInvestmentAccountPortfolio(investmentAccountId: Long): Mono<InvestmentAccountDTO> {
        // 1. Load Investmentaccount
        val accountMono = investmentAccountRepository.findById(investmentAccountId)
            .switchIfEmpty(Mono.error(RuntimeException("InvestmentAccount not found")))

        // 2. Load Owner and create OwnerDTO
        val ownerMono = accountMono.flatMap { account ->
            ownerRepository.findById(account.ownerId!!).map { owner ->
                OwnerDTO(
                    id = owner.id, name = owner.name, mail = owner.mail
                )
            }
        }

        // Load BankAccount and create BankAccountDTO
        val bankAccountMono = accountMono.flatMap { account ->
            bankAccountRepository.findById(account.bankAccountId!!).map { bankAccount ->
                BankAccountDTO(
                    id = bankAccount.id, currency = bankAccount.currency, balance = bankAccount.balance
                )
            }
        }

        val portfolioMono = accountMono.flatMap { account ->
            portfolioEntryRepository.findByInvestmentAccountId(account.id!!).flatMap { portfolioEntry ->
                stockRepository.findBySymbol(portfolioEntry.stockSymbol).flatMap { stock ->
                    latestIStockQuoteRepository.findById(portfolioEntry.stockSymbol).flatMap { latestStockQuote ->
                        quoteRepository.findById(latestStockQuote.quoteId).map { quote ->
                            PortfolioEntryDTO.mapToDto(
                                portfolioEntry, StockDTO.mapToDto(stock, quote)
                            )
                        }
                    }
                }
            }.collectList()
        }

        val totalValue = calculateInvestmentAccountTotalValueAsync(portfolioMono)

        // Combine all and create InvestmentAccountDTO
        return Mono.zip(accountMono, ownerMono, bankAccountMono, portfolioMono, totalValue).map { tuple ->
            val account = tuple.component1()
            val owner = tuple.component2()
            val bankAccount = tuple.component3()
            val portfolio = tuple.component4()
            val portfolioTotalValue = tuple.component5()

            val roundedPortfolioTotalValue = portfolioTotalValue.setScale(2, RoundingMode.HALF_UP)

            InvestmentAccountDTO.mapToDto(account.id!!, bankAccount, owner, portfolio, roundedPortfolioTotalValue)
        }
    }

    private fun calculateInvestmentAccountTotalValueAsync(portfolioMono: Mono<List<PortfolioEntryDTO>>): Mono<BigDecimal> {
        return portfolioMono.flatMap { portfolioEntries ->
            Flux.fromIterable(portfolioEntries).flatMap { entry ->
                finnhubHandler.fetchStockQuote(entry.stockSymbol).map { quote ->
                    quote.currentPrice.multiply(BigDecimal(entry.quantity))
                }
            }.reduce(BigDecimal.ZERO) { acc, value -> acc.add(value) }
        }
    }

}