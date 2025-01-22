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

/**
 * Unit tests for the [InvestmentAccountService] class.
 *
 * This class verifies the functionality of `InvestmentAccountService` methods using mocked dependencies
 * for the repository and transaction operator.
 */
class InvestmentAccountServiceTest {
    private val investmentAccountRepository: IInvestmentAccountRepository = mock(IInvestmentAccountRepository::class.java)
    private val portfolioEntryRepository: IPortfolioEntryRepository = mock(IPortfolioEntryRepository::class.java)
    private val bankAccountRepository: IBankAccountRepository = mock(IBankAccountRepository::class.java)
    private val ownerRepository: IOwnerRepository = mock(IOwnerRepository::class.java)
    private val stockRepository: IStockRepository = mock(IStockRepository::class.java)
    private val finnhubHandler: FinnhubHandler = mock(FinnhubHandler::class.java)
    private val stockService: StockService = mock(StockService::class.java)
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
        portfolioEntry = PortfolioEntry(investmentAccountId = investmentAccount.id!!, stockSymbol = stock.symbol, id = 1L, quantity = 10.0)

        `when`(investmentAccountRepository.findByOwnerId(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.save(any())).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findByBankAccountId(bankAccount.id!!)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findById(investmentAccount.id!!)).thenReturn(Mono.just(investmentAccount))
        `when`(stockRepository.findBySymbol(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(stockRepository.findById(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(bankAccountRepository.findById(bankAccount.id!!)).thenReturn(Mono.just(bankAccount))
        `when`(portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(investmentAccount.id!!, stockSymbol)).thenReturn(Mono.just(portfolioEntry))
        `when`(portfolioEntryRepository.findByInvestmentAccountId(investmentAccount.id!!)).thenReturn(Flux.just(portfolioEntry))
        `when`(portfolioEntryRepository.save(any())).thenAnswer { Mono.just(portfolioEntry) }
        `when`(bankAccountRepository.save(any())).thenAnswer { Mono.just(bankAccount) }
        `when`(finnhubHandler.fetchStockQuote(stockSymbol)).thenReturn(Mono.just(stockQuote))
        `when`(ownerRepository.findById(owner.id)).thenReturn(Mono.just(owner))
        `when`(stockService.getLatestQuoteBySymbol(stockSymbol)).thenReturn(Mono.just(stockQuote))

        investmentAccountService = InvestmentAccountService(operator, investmentAccountRepository, portfolioEntryRepository,
            bankAccountRepository, ownerRepository, stockRepository, finnhubHandler, stockService)
    }

    /**
     * Tests the [InvestmentAccountService.sellStock] method to ensure that selling stock updates the portfolio correctly.
     *
     * TODO add once implemented
     */
    @Test
    fun `test sell stock`() {
        val result = investmentAccountService.sellStock(investmentAccount.id!!, stock.symbol, BigDecimal(10)).block()

        assertNotNull(result)
        assertEquals(investmentAccount.id!!, result!!.id)
        verify(bankAccountRepository).findById(1L)
        verify(portfolioEntryRepository).findByInvestmentAccountIdAndStockSymbol(investmentAccount.id!!, stock.symbol)
        verify(portfolioEntryRepository).save(any())
        verify(bankAccountRepository).save(any())
    }

    /**
     * Tests the [InvestmentAccountService.buyStock] method to ensure that buying stock updates the portfolio correctly.
     *
     * TODO add once implemented
     */
    @Test
    fun `test buy stock`() {
        val result = investmentAccountService.buyStock(investmentAccount.id!!, stock.symbol, BigDecimal(10)).block()

        assertNotNull(result)
        assertEquals(investmentAccount.id!!, result!!.id)
        verify(bankAccountRepository).findById(1L)
        verify(portfolioEntryRepository).findByInvestmentAccountIdAndStockSymbol(investmentAccount.id!!, stock.symbol)
        verify(portfolioEntryRepository).save(any())
        verify(bankAccountRepository).save(any())
    }

    /**
     * Tests the [InvestmentAccountService.getInvestmentAccountPortfolio] method to ensure that it retrieves the correct investment account.
     */
    @Test
    fun `test getInvestmentAccountPortfolio returns the correct DTO`() {
        val result = investmentAccountService.getInvestmentAccountPortfolio(1L).block()

        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals(1L, result.bankAccountId)
        assertEquals(1, result.portfolio.size)
        assertEquals(OwnerDTO.mapToDto(owner), result.owner)
        //assertEquals(StockDTO.mapToDto(stock, stockQuote), result.portfolio[0].stock)           // TODO wie vergleicht man Big Decimal
    }


}