package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.ROUNDING_NUMBER_TO_DECIMAL_PLACE
import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.entities.Owner
import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
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
        return investmentAccountRepository.findById(investmentAccountId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Investment account not found")))
            .flatMap { investmentAccount ->
                validateBankAccount(investmentAccount).flatMap {
                    fetchStockQuote(stockSymbol).flatMap { quote ->
                        handlePortfolioUpdate(investmentAccountId, stockSymbol, purchaseAmount, quote).flatMap {
                            updateBankAccountForPurchase(investmentAccount, purchaseAmount)
                        }
                    }
                }
            }
    }

    /**
     * Fetches the stock quote for the given stock symbol.
     */
    private fun fetchStockQuote(stockSymbol: String): Mono<StockQuote> {
        return latestIStockQuoteRepository.findById(stockSymbol)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Stock quote not found")))
            .flatMap { latestStockQuote ->
                quoteRepository.findById(latestStockQuote.quoteId)
                    .switchIfEmpty(Mono.error(IllegalArgumentException("Quote not found")))
            }
    }

    /**
     * Updates the portfolio by either creating a new entry or updating an existing one.
     */
    private fun handlePortfolioUpdate(
        investmentAccountId: Long,
        stockSymbol: String,
        purchaseAmount: BigDecimal,
        quote: StockQuote
    ): Mono<PortfolioEntry> {
        val calculatedVolume = calculateVolume(purchaseAmount, quote.currentPrice)
        return portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
            .flatMap { existingEntry ->
                updateExistingPortfolioEntry(existingEntry, calculatedVolume, purchaseAmount)
            }
            .switchIfEmpty(createNewPortfolioEntry(investmentAccountId, stockSymbol, calculatedVolume, purchaseAmount))
    }

    /**
     * Calculates the volume of stock that can be purchased based on the purchase amount and stock price.
     */
    private fun calculateVolume(purchaseAmount: BigDecimal, stockPrice: BigDecimal): BigDecimal {
        return purchaseAmount.divide(stockPrice, ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP)
    }

    /**
     * Updates an existing portfolio entry with the new purchase details.
     */
    private fun updateExistingPortfolioEntry(
        existingEntry: PortfolioEntry,
        calculatedVolume: BigDecimal,
        purchaseAmount: BigDecimal
    ): Mono<PortfolioEntry> {
        val updatedQuantity = existingEntry.quantity + calculatedVolume.toDouble()
        val updatedEntry = existingEntry.copy(
            quantity = updatedQuantity,
            totalInvestAmount = existingEntry.totalInvestAmount.plus(purchaseAmount)
        )
        return portfolioEntryRepository.save(updatedEntry).`as`(operator::transactional)
    }

    /**
     * Creates a new portfolio entry for the stock purchase.
     */
    private fun createNewPortfolioEntry(
        investmentAccountId: Long,
        stockSymbol: String,
        calculatedVolume: BigDecimal,
        purchaseAmount: BigDecimal
    ): Mono<PortfolioEntry> {
        val newEntry = PortfolioEntry(
            investmentAccountId = investmentAccountId,
            stockSymbol = stockSymbol,
            quantity = calculatedVolume.toDouble(),
            totalInvestAmount = purchaseAmount
        )
        return portfolioEntryRepository.save(newEntry).`as`(operator::transactional)
    }

    /**
     * Updates the linked bank account by deducting the purchase amount.
     */
    private fun updateBankAccountForPurchase(
        investmentAccount: InvestmentAccount,
        purchaseAmount: BigDecimal
    ): Mono<InvestmentAccount> {
        return bankAccountRepository.findById(investmentAccount.bankAccountId!!)
            .flatMap { bankAccount ->
                val updatedBalance = bankAccount.balance.minus(purchaseAmount)
                if (updatedBalance < BigDecimal.ZERO) {
                    return@flatMap Mono.error<InvestmentAccount>(
                        IllegalArgumentException(
                            "Investment account ${investmentAccount.id}: Insufficient balance in the bank account to buy $purchaseAmount"
                        )
                    )
                }
                val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                bankAccountRepository.save(updatedBankAccount).`as`(operator::transactional)
            }
            .thenReturn(investmentAccount)
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
                validateBankAccount(investmentAccount).flatMap {
                    processStockSale(investmentAccount, stockSymbol, volume)
                }
            }
    }

    /**
     * Validates that the investment account has a linked bank account.
     */
    private fun validateBankAccount(investmentAccount: InvestmentAccount): Mono<Unit> {
        return if (investmentAccount.bankAccountId == null) {
            Mono.error(IllegalArgumentException("No bank account linked to the investment account"))
        } else {
            Mono.just(Unit)
        }
    }

    /**
     * Processes the stock sale, including validation, portfolio update, and bank account update.
     */
    private fun processStockSale(
        investmentAccount: InvestmentAccount, stockSymbol: String, volume: Double
    ): Mono<InvestmentAccount> {
        return portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccount.id!!, stockSymbol)
            .switchIfEmpty(Mono.error(IllegalArgumentException("Stock not found in portfolio")))
            .flatMap { existingEntry ->
                fetchStockQuote(existingEntry.stockSymbol)
                    .flatMap { quote ->
                        validateSufficientStock(existingEntry, volume).flatMap {
                            performStockSale(existingEntry, investmentAccount, volume, quote)
                        }
                    }
            }
    }

    /**
     * Validates that there is sufficient stock quantity to sell.
     */
    private fun validateSufficientStock(existingEntry: PortfolioEntry, volume: Double): Mono<Unit> {
        return if (existingEntry.quantity < volume) {
            Mono.error(IllegalArgumentException("Insufficient stock quantity to sell"))
        } else {
            Mono.just(Unit)
        }
    }

    /**
     * Performs the stock sale by updating the portfolio and linked bank account.
     */
    private fun performStockSale(
        existingEntry: PortfolioEntry, investmentAccount: InvestmentAccount, volume: Double, quote: StockQuote
    ): Mono<InvestmentAccount> {
        val newQuantity = existingEntry.quantity - volume
        val investReduction = calculateInvestReduction(existingEntry, volume)
        val newTotalInvestAmount = existingEntry.totalInvestAmount.subtract(investReduction)

        val portfolioUpdate = if (newQuantity > 0) {
            val updatedEntry = existingEntry.copy(
                quantity = newQuantity,
                totalInvestAmount = newTotalInvestAmount
            )
            portfolioEntryRepository.save(updatedEntry)
        } else {
            portfolioEntryRepository.delete(existingEntry).then(Mono.empty())
        }

        val soldAmount = quote.currentPrice.multiply(volume.toBigDecimal())

        return portfolioUpdate
            .then(updateBankAccount(investmentAccount, soldAmount))
    }

    /**
     * Calculates the amount to reduce from the total investment based on the volume sold.
     */
    private fun calculateInvestReduction(existingEntry: PortfolioEntry, volume: Double): BigDecimal {
        return existingEntry.totalInvestAmount.multiply(
            volume.toBigDecimal().divide(
                existingEntry.quantity.toBigDecimal(),
                ROUNDING_NUMBER_TO_DECIMAL_PLACE,
                RoundingMode.HALF_UP
            )
        )
    }

    /**
     * Updates the linked bank account by adding the sale proceeds.
     */
    private fun updateBankAccount(
        investmentAccount: InvestmentAccount,
        soldAmount: BigDecimal
    ): Mono<InvestmentAccount> {
        return bankAccountRepository.findById(investmentAccount.bankAccountId!!)
            .flatMap { bankAccount ->
                val updatedBalance = bankAccount.balance.add(soldAmount)
                val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                bankAccountRepository.save(updatedBankAccount)
            }
            .thenReturn(investmentAccount)
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