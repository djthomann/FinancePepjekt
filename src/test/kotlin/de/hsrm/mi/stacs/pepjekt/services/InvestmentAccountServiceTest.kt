package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.entities.dtos.*
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers.any
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.*
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.math.RoundingMode

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
    private lateinit var investmentAccountDTO: InvestmentAccountDTO
    private lateinit var owner: Owner

    /**
     * Initializes the test environment by setting up mock data and dependencies.
     */
    @BeforeEach
    fun setUp() {
        val stockSymbol = "AAPL"
        bankAccount = BankAccount(
            id = 1L,
            balance = BigDecimal(150),
            currency = Currency.USD
        )
        owner = Owner(
            id = 1L,
            name = "Hans",
            mail = "hans@example.com",
        )
        stock = Stock(stockSymbol, "Apple Inc.", "BBG000B9XRY4", "figi", Currency.USD)
        stockQuote = StockQuote(
            currentPrice = BigDecimal(100),
            timeStamp = LocalDateTime.now().minusDays(1),
            stockSymbol = stock.symbol,
            id = 1L,
            change = 10.0F,
            percentChange = 10.0F,
            highPriceOfTheDay = BigDecimal(120),
            lowPriceOfTheDay = BigDecimal(70),
            openPriceOfTheDay = BigDecimal(100),
            previousClosePrice = BigDecimal(100)
        )
        investmentAccount = InvestmentAccount(bankAccountId = bankAccount.id, id = 1L, ownerId = 1L)
        portfolioEntry = PortfolioEntry(
            investmentAccountId = investmentAccount.id!!, stockSymbol = stock.symbol, id
            = 1L, quantity = 10.0, totalInvestAmount = 1000.0.toBigDecimal()
        )

        `when`(investmentAccountRepository.findByOwnerId(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.save(any())).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findByBankAccountId(bankAccount.id!!)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findById(investmentAccount.id!!)).thenReturn(Mono.just(investmentAccount))
        `when`(stockRepository.findBySymbol(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(stockRepository.findById(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(bankAccountRepository.findById(bankAccount.id!!)).thenReturn(Mono.just(bankAccount))
        `when`(
            portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(
                investmentAccount.id!!,
                stockSymbol
            )
        ).thenReturn(Mono.just(portfolioEntry))
        `when`(portfolioEntryRepository.findByInvestmentAccountId(investmentAccount.id!!)).thenReturn(
            Flux.just(
                portfolioEntry
            )
        )
        `when`(portfolioEntryRepository.save(any())).thenAnswer { Mono.just(portfolioEntry) }
        `when`(bankAccountRepository.save(any())).thenAnswer { Mono.just(bankAccount) }
        `when`(finnhubHandler.fetchStockQuote(stockSymbol)).thenReturn(Mono.just(stockQuote))
        `when`(ownerRepository.findById(owner.id)).thenReturn(Mono.just(owner))
        `when`(stockService.getLatestQuoteBySymbol(stockSymbol)).thenReturn(Mono.just(stockQuote))

        `when`(operator.transactional(any(Mono::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Mono<PortfolioEntry>>(0)
        }

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


    @Test
    fun `test buyStock successfully updates portfolio and deducts balance`() {
        val investmentAccountId = 1L
        val stockSymbol = "AAPL"
        val purchaseAmount = BigDecimal(200)
        val currentStockPrice = BigDecimal(100)
        val calculatedVolume = purchaseAmount.divide(currentStockPrice, RoundingMode.HALF_UP)

        val investmentAccount = InvestmentAccount(id = investmentAccountId, bankAccountId = 1L)
        val bankAccount = BankAccount(id = 1L, currency = Currency.USD, balance = BigDecimal(500))

        val stockQuote = StockQuote(
            id = 1L, currentPrice = currentStockPrice, change = 0f, percentChange = 0f,
            highPriceOfTheDay = BigDecimal(110), lowPriceOfTheDay = BigDecimal(90),
            openPriceOfTheDay = BigDecimal(100), previousClosePrice = BigDecimal(99),
            timeStamp = LocalDateTime.now(), stockSymbol = stockSymbol
        )
        val portfolioEntry = PortfolioEntry(
            id = null, investmentAccountId = investmentAccountId, stockSymbol = stockSymbol,
            quantity = calculatedVolume.toDouble(), totalInvestAmount = purchaseAmount
        )

        val latestQuote = StockQuoteLatest(
            stockSymbol = "AAPL",
            quoteId = 1L
        )

        `when`(investmentAccountRepository.findById(investmentAccountId)).thenReturn(Mono.just(investmentAccount))
        `when`(bankAccountRepository.findById(1L)).thenReturn(Mono.just(bankAccount))
        doReturn(Mono.just(latestQuote))
            .`when`(latestIStockQuoteRepository).findById(stockSymbol)

        `when`(quoteRepository.findById(1L)).thenReturn(Mono.just(stockQuote))
        doReturn(Mono.empty<PortfolioEntry>())
            .`when`(portfolioEntryRepository)
            .findByInvestmentAccountIdAndStockSymbol(investmentAccountId, stockSymbol)


        doReturn(Mono.just(portfolioEntry)).`when`(portfolioEntryRepository)
            .save(any(PortfolioEntry::class.java))

        doReturn(Mono.just(bankAccount.copy(balance = BigDecimal(300))))
            .`when`(bankAccountRepository)
            .save(any(BankAccount::class.java))


        val result = investmentAccountService.buyStock(investmentAccountId, stockSymbol, purchaseAmount).block()

        assertNotNull(result)
        assertEquals(investmentAccountId, result?.id)
        verify(investmentAccountRepository).findById(investmentAccountId)
        verify(bankAccountRepository).save(argThat { it.balance == BigDecimal(300) })
        verify(portfolioEntryRepository).save(argThat {
            it.investmentAccountId == investmentAccountId &&
                    it.stockSymbol == stockSymbol &&
                    it.quantity == calculatedVolume.toDouble() &&
                    it.totalInvestAmount == purchaseAmount
        })
    }

}