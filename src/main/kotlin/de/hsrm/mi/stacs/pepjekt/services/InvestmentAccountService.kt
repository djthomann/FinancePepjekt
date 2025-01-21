package de.hsrm.mi.stacs.pepjekt.services

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
    val stockService: StockService
) : IInvestmentAccountService {

    /**
     * Buys a stock and updates the investment account portfolio and bank account accordingly.
     *
     * @param investmentAccountId the ID of the investment account making the purchase
     * @param stockSymbol the symbol of the stock to purchase
     * @param volume the volume of the stock to purchase
     * @return a [Mono] emitting the updated [InvestmentAccount] or an error if any operation fails
     */
    override fun buyStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Mono<InvestmentAccount> {
        return investmentAccountRepository.findById(investmentAccountId)
            .flatMap { investmentAccount ->
                if (investmentAccount.bankAccountId == null) {
                    return@flatMap Mono.error<InvestmentAccount>(IllegalArgumentException("No bank account linked to the investment account"))
                }

                portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
                    .flatMap { existingEntry ->
                        // Update the existing entry
                        val updatedQuantity = existingEntry.quantity + volume.toDouble()
                        val updatedEntry = existingEntry.copy(quantity = updatedQuantity)
                        portfolioEntryRepository.save(updatedEntry).`as`(operator::transactional)
                    }
                    .switchIfEmpty(
                        // Create a new entry if none exists
                        portfolioEntryRepository.save(
                            PortfolioEntry(
                                investmentAccountId = investmentAccountId,
                                stockSymbol = stockSymbol,
                                quantity = volume.toDouble(),
                            )
                        ).`as`(operator::transactional)
                    )
                    .flatMap {
                        // Update the balance in the linked bank account
                        bankAccountRepository.findById(investmentAccount.bankAccountId)
                    }
                    .flatMap { bankAccount ->
                        val updatedBalance = bankAccount.balance.minus(volume)
                        if (updatedBalance < BigDecimal.ZERO) {
                            return@flatMap Mono.error<InvestmentAccount>(
                                IllegalArgumentException(
                                    "Insufficient balance " +
                                            "in the bank account"
                                )
                            )
                        }

                        val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                        bankAccountRepository.save(updatedBankAccount).`as`(operator::transactional)
                    }
                    .thenReturn(investmentAccount)
            }
    }


    /**
     * Sells a stock and updates the investment account portfolio and bank account balance.
     *
     * @param investmentAccountId the ID of the investment account making the sale
     * @param stockSymbol the symbol of the stock to sell
     * @param volume the volume of the stock to sell
     * @return a [Mono] emitting the updated [InvestmentAccount] or an error if any operation fails
     */
    override fun sellStock(
        investmentAccountId: Long,
        stockSymbol: String,
        volume: BigDecimal
    ): Mono<InvestmentAccount> {
        return investmentAccountRepository.findById(investmentAccountId)
            .flatMap { investmentAccount ->
                if (investmentAccount.bankAccountId == null) {
                    return@flatMap Mono.error<InvestmentAccount>(IllegalArgumentException("No bank account linked to the investment account"))
                }

                portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)
                    .flatMap { existingEntry ->
                        if (existingEntry == null) {
                            return@flatMap Mono.error<InvestmentAccount>(IllegalArgumentException("Stock not found in portfolio"))
                        }

                        val newQuantity = existingEntry.quantity - volume.toDouble()
                        if (newQuantity < 0) {
                            return@flatMap Mono.error<InvestmentAccount>(IllegalArgumentException("Insufficient stock quantity to sell"))
                        }

                        // Update existing entry or create a new one with 0 quantity (effectively removing)
                        val updatedEntry: PortfolioEntry
                        if (newQuantity > 0) {
                            updatedEntry = existingEntry.copy(quantity = newQuantity)
                        } else {
                            return@flatMap portfolioEntryRepository.delete(existingEntry).`as`(operator::transactional)
                                .then(Mono.just(investmentAccount))
                        }

                        portfolioEntryRepository.save(updatedEntry).`as`(operator::transactional)
                    }
                    .flatMap {
                        // Update the balance in the linked bank account
                        bankAccountRepository.findById(investmentAccount.bankAccountId)
                    }
                    .flatMap { bankAccount ->
                        val updatedBalance = bankAccount.balance.plus(volume)
                        val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                        bankAccountRepository.save(updatedBankAccount).`as`(operator::transactional)
                    }
                    .thenReturn(investmentAccount)
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
            ownerRepository.findById(account.ownerId!!)
                .map { owner ->
                    OwnerDTO(
                        id = owner.id,
                        name = owner.name,
                        mail = owner.mail
                    )
                }
        }

        // Load BankAccount and create BankAccountDTO
        val bankAccountMono = accountMono.flatMap { account ->
            bankAccountRepository.findById(account.bankAccountId!!)
                .map { bankAccount ->
                    BankAccountDTO(
                        id = bankAccount.id,
                        currency = bankAccount.currency,
                        balance = bankAccount.balance
                    )
                }
        }

        // Load Portfolio and create PortfolioDTO
        val portfolioMono = accountMono.flatMap { account ->
            portfolioEntryRepository.findByInvestmentAccountId(account.id!!)
                .flatMap { portfolioEntry ->
                    // Lade Stock-Daten für jedes PortfolioEntry
                    stockRepository.findBySymbol(portfolioEntry.stockSymbol).flatMap { stock ->
                        stockService.getLatestQuoteBySymbol(stock.symbol)
                            .map { quote ->
                                PortfolioEntryDTO(
                                    id = portfolioEntry.id,
                                    stockSymbol = portfolioEntry.stockSymbol,
                                    quantity = portfolioEntry.quantity,
                                    stock = StockDTO.mapToDto(stock, quote)
                                )
                            }
                    }
                }.collectList()
        }

        val totalValue = calculateInvestmentAccountTotalValueAsync(portfolioMono)

        // Combine all and create InvestmentAccountDTO
        return Mono.zip(accountMono, ownerMono, bankAccountMono, portfolioMono, totalValue)
            .map { tuple ->
                val account = tuple.component1()
                val owner = tuple.component2()
                val bankAccount = tuple.component3()
                val portfolio = tuple.component4()
                val portfolioTotalValue = tuple.component5()

                val roundedPortfolioTotalValue = portfolioTotalValue.setScale(2, RoundingMode.HALF_UP)

                InvestmentAccountDTO(
                    id = account.id,
                    bankAccountId = account.bankAccountId,
                    portfolio = portfolio,
                    bankAccount = bankAccount,
                    owner = owner,
                    totalValue = roundedPortfolioTotalValue
                )
            }
    }

    /**
     * Returns an investment account by its id
     */
    override fun getInvestmentAccount(investmentAccountId: Long): Mono<InvestmentAccount> {
        return investmentAccountRepository.findById(investmentAccountId)
    }

    // Asynchrone Methode, die den Gesamtwert des Depots berechnet
    private fun calculateInvestmentAccountTotalValueAsync(portfolioMono: Mono<List<PortfolioEntryDTO>>): Mono<BigDecimal> {
        return portfolioMono.flatMap { portfolioEntries ->
            Flux.fromIterable(portfolioEntries)
                .flatMap { entry ->
                    // Hole aktuelle Quote für das Stock-Symbol
                    finnhubHandler.fetchStockQuote(entry.stockSymbol)
                        .map { quote ->
                            // Berechne den Wert der Position
                            quote.currentPrice.multiply(BigDecimal(entry.quantity))
                        }
                }
                .reduce(BigDecimal.ZERO) { acc, value -> acc.add(value) } // Summiere alle Werte
        }
    }

    fun getInvestmentAccountOwner(userId: Long): Mono<Owner> {
        return ownerRepository.findById(userId)
    }

}