package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import reactor.core.publisher.Flux

/**
 * Unit tests for the [InvestmentAccountService] class.
 *
 * This class verifies the functionality of `InvestmentAccountService` methods using mocked dependencies
 * for the repository and transaction operator.
 */
class InvestmentAccountServiceTest {

    private val investmentAccountRepository: IInvestmentAccountRepository =
        mock(IInvestmentAccountRepository::class.java)
    private val portfolioEntryRepository: IPortfolioEntryRepository = mock(IPortfolioEntryRepository::class.java)
    private val bankAccountRepository: IBankAccountRepository = mock(IBankAccountRepository::class.java)
    private val ownerRepository: IOwnerRepository = mock(IOwnerRepository::class.java)
    private val stockRepository: IStockRepository = mock(IStockRepository::class.java)
    private val finnhubHandler: FinnhubHandler = mock(FinnhubHandler::class.java)
    private val stockService: StockService = mock(StockService::class.java)
    private val latestIStockQuoteRepository: IStockQuoteLatestRepository = mock(IStockQuoteLatestRepository::class.java)
    private val quoteRepository: IStockQuoteRepository = mock(IStockQuoteRepository::class.java)
    private val operator: TransactionalOperator = mock(TransactionalOperator::class.java)

    private lateinit var investmentAccountService: InvestmentAccountService
    private lateinit var bankAccount: BankAccount
    private lateinit var investmentAccount: InvestmentAccount
    private lateinit var stock: Stock
    private lateinit var stockQuote: StockQuote
    private lateinit var portfolioEntry: PortfolioEntry
    private lateinit var owner: Owner

    /**
     * Initializes the test environment by setting up mock data and dependencies.
     */
    @BeforeEach
    fun setUp() {
        bankAccount = createBankAccount()
        owner = createOwner()
        stock = createStock("AAPL")
        stockQuote = createStockQuote("AAPL", BigDecimal(100))
        investmentAccount = createInvestmentAccount()
        portfolioEntry = createPortfolioEntry()

        mockRepositories()
        mockTransactionalOperator()

        investmentAccountService = InvestmentAccountService(
            operator,
            investmentAccountRepository,
            portfolioEntryRepository,
            bankAccountRepository,
            ownerRepository,
            stockRepository,
            finnhubHandler,
            latestIStockQuoteRepository,
            quoteRepository
        )
    }

    private fun createBankAccount() = BankAccount(
        id = 1L, balance = BigDecimal(150), currency = Currency.USD
    )

    private fun createOwner() = Owner(
        id = 1L, name = "Hans", mail = "hans@example.com"
    )

    private fun createStock(symbol: String) = Stock(symbol, "Apple Inc.", "BBG000B9XRY4", "figi", Currency.USD)

    private fun createStockQuote(stockSymbol: String, price: BigDecimal) = StockQuote(
        currentPrice = price,
        timeStamp = LocalDateTime.now().minusDays(1),
        stockSymbol = stockSymbol,
        id = 1L,
        change = 10.0F,
        percentChange = 10.0F,
        highPriceOfTheDay = BigDecimal(120),
        lowPriceOfTheDay = BigDecimal(70),
        openPriceOfTheDay = BigDecimal(100),
        previousClosePrice = BigDecimal(100)
    )

    private fun createInvestmentAccount() = InvestmentAccount(bankAccountId = bankAccount.id, id = 1L, ownerId = 1L)

    private fun createPortfolioEntry() = PortfolioEntry(
        investmentAccountId = investmentAccount.id!!,
        stockSymbol = stock.symbol,
        id = 1L,
        quantity = 10.0,
        totalInvestAmount = 1000.0.toBigDecimal()
    )

    private fun mockRepositories() {
        `when`(investmentAccountRepository.findByOwnerId(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.save(any())).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findByBankAccountId(bankAccount.id!!)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findById(investmentAccount.id!!)).thenReturn(Mono.just(investmentAccount))
        `when`(stockRepository.findBySymbol("AAPL")).thenReturn(Mono.just(stock))
        `when`(stockRepository.findById("AAPL")).thenReturn(Mono.just(stock))
        `when`(bankAccountRepository.findById(bankAccount.id!!)).thenReturn(Mono.just(bankAccount))
        `when`(
            portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(
                investmentAccount.id!!, "AAPL"
            )
        ).thenReturn(Mono.just(portfolioEntry))
        `when`(portfolioEntryRepository.findByInvestmentAccountId(investmentAccount.id!!)).thenReturn(
            Flux.just(
                portfolioEntry
            )
        )
        `when`(portfolioEntryRepository.save(any())).thenReturn(Mono.just(portfolioEntry))
        `when`(finnhubHandler.fetchStockQuote("AAPL")).thenReturn(Mono.just(stockQuote))
        `when`(ownerRepository.findById(owner.id)).thenReturn(Mono.just(owner))
        `when`(stockService.getLatestQuoteBySymbol("AAPL")).thenReturn(Mono.just(stockQuote))
    }

    private fun mockTransactionalOperator() {
        `when`(operator.transactional(any(Mono::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Mono<PortfolioEntry>>(0)
        }
    }

    @Test
    fun `test buyStock successfully updates portfolio and deducts balance`() {
        val investmentAccountId = 1L
        val stockSymbol = "AAPL"
        val purchaseAmount = BigDecimal(200)
        val currentStockPrice = BigDecimal(100)
        val calculatedVolume = purchaseAmount.divide(currentStockPrice, RoundingMode.HALF_UP)

        val bankAccount = createBankAccount().copy(balance = BigDecimal(500))
        val portfolioEntry = PortfolioEntry(
            id = null,
            investmentAccountId = investmentAccountId,
            stockSymbol = stockSymbol,
            quantity = calculatedVolume.toDouble(),
            totalInvestAmount = purchaseAmount
        )

        val latestQuote = StockQuoteLatest(stockSymbol = stockSymbol, quoteId = 1L)

        `when`(investmentAccountRepository.findById(investmentAccountId)).thenReturn(Mono.just(investmentAccount))
        `when`(bankAccountRepository.findById(1L)).thenReturn(Mono.just(bankAccount))
        doReturn(Mono.just(latestQuote)).`when`(latestIStockQuoteRepository).findById(stockSymbol)
        `when`(quoteRepository.findById(1L)).thenReturn(Mono.just(stockQuote))
        doReturn(Mono.empty<PortfolioEntry>()).`when`(portfolioEntryRepository)
            .findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)

        doReturn(Mono.just(portfolioEntry)).`when`(portfolioEntryRepository).save(any(PortfolioEntry::class.java))
        doReturn(Mono.just(bankAccount.copy(balance = BigDecimal(300)))).`when`(bankAccountRepository)
            .save(any(BankAccount::class.java))

        val result = investmentAccountService.buyStock(investmentAccountId, stockSymbol, purchaseAmount).block()

        assertNotNull(result)
        assertEquals(investmentAccountId, result?.id)
        verify(investmentAccountRepository).findById(investmentAccountId)
        verify(bankAccountRepository).save(argThat { it.balance == BigDecimal(300) })
        verify(portfolioEntryRepository).save(argThat {
            it.investmentAccountId == investmentAccountId && it.stockSymbol == stockSymbol && it.quantity == calculatedVolume.toDouble() && it.totalInvestAmount == purchaseAmount
        })
    }
}
