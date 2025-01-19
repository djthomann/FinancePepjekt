package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.entities.dtos.PortfolioEntryDTO
import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDTO
import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDetailsDTO
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test

/**
 * Unit tests for the [StockService] class.
 *
 * This class ensures the proper functioning of the methods within the `StockService` class
 * by testing various scenarios with mocked dependencies.
 */
class StockServiceTest {

    private val stockRepository: IStockRepository = mock(IStockRepository::class.java)
    private val stockQuoteRepository: IStockQuoteRepository = mock(IStockQuoteRepository::class.java)
    private val investmentAccountRepository: IInvestmentAccountRepository = mock(IInvestmentAccountRepository::class.java)
    private val stockQuoteLatestRepository: IStockQuoteLatestRepository = mock(IStockQuoteLatestRepository::class.java)
    private val portfolioEntryRepository: IPortfolioEntryRepository = mock(IPortfolioEntryRepository::class.java)
    private val databaseClient: DatabaseClient = mock(DatabaseClient::class.java)

    private lateinit var stockService: StockService
    private lateinit var stock: Stock
    private lateinit var stockQuotes: List<StockQuote>
    private lateinit var portfolioEntry: PortfolioEntry
    private lateinit var investmentAccount: InvestmentAccount
    private lateinit var stockQuoteLatest: StockQuoteLatest

    /**
     * Sets up test environment by initializing mocked repositories, defining their behaviors,
     * and preparing test data for the Stock and Quote entities.
     */
    @BeforeEach
    fun setUp() {
        stock = Stock(symbol = "AAPL", description = "Apple Inc.", figi = "BBG000B9XRY4", currency = Currency.USD, name = "Apple")
        investmentAccount = InvestmentAccount(
            bankAccountId = 1L,
            ownerId = 1L,
            id = 1L
        )
        stockQuotes = listOf(
            StockQuote(
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
            ),
            StockQuote(
                currentPrice = BigDecimal(100),
                timeStamp = LocalDateTime.now().minusDays(21),
                stockSymbol = stock.symbol,
                id = 2L,
                change = 10.0F,
                percentChange = 10.0F,
                highPriceOfTheDay = BigDecimal(150),
                lowPriceOfTheDay = BigDecimal(40),
                openPriceOfTheDay = BigDecimal(100),
                previousClosePrice = BigDecimal(90)
            ),
            StockQuote(
                currentPrice = BigDecimal(150),
                timeStamp = LocalDateTime.now().minusDays(5),
                stockSymbol = stock.symbol,
                id = 3L,
                change = 10.0F,
                percentChange = 10.0F,
                highPriceOfTheDay = BigDecimal(110),
                lowPriceOfTheDay = BigDecimal(55),
                openPriceOfTheDay = BigDecimal(190),
                previousClosePrice = BigDecimal(100)
            ),
            StockQuote(
                currentPrice = BigDecimal(300),
                timeStamp = LocalDateTime.now().minusHours(1),
                stockSymbol = stock.symbol,
                id = 4L,
                change = 8.0F,
                percentChange = 12.0F,
                highPriceOfTheDay = BigDecimal(120),
                lowPriceOfTheDay = BigDecimal(90),
                openPriceOfTheDay = BigDecimal(100),
                previousClosePrice = BigDecimal(110)
            ),
            StockQuote(
                currentPrice = BigDecimal(350),
                timeStamp = LocalDateTime.now().minusHours(15),
                stockSymbol = stock.symbol,
                id = 5L,
                change = 2.0F,
                percentChange = 4.2F,
                highPriceOfTheDay = BigDecimal(130),
                lowPriceOfTheDay = BigDecimal(45),
                openPriceOfTheDay = BigDecimal(90),
                previousClosePrice = BigDecimal(110)
            )
        )
        stockQuoteLatest = StockQuoteLatest(stockSymbol = stock.symbol, quote_id = 1L)
        portfolioEntry = PortfolioEntry(investmentAccountId = investmentAccount.id!!, stockSymbol = stock.symbol, id = 1L, quantity = 10.0)

        stockService = StockService(stockRepository, stockQuoteRepository, stockQuoteLatestRepository, databaseClient, investmentAccountRepository, portfolioEntryRepository)
        `when`(stockRepository.findBySymbol(stock.symbol)).thenReturn(Mono.just(stock))
        `when`(stockQuoteRepository.findByStockSymbol(stock.symbol)).thenReturn(Flux.fromIterable(stockQuotes))
    }

    @Test
    fun `test getStockDetails`() {
        `when`(stockQuoteLatestRepository.findById(stock.symbol)).thenReturn(Mono.just(stockQuoteLatest))
        `when`(stockQuoteRepository.findById(stockQuoteLatest.quote_id)).thenReturn(Mono.just(stockQuotes[0]))
        `when`(stockService.getLatestQuoteBySymbol(stock.symbol)).thenReturn(Mono.just(stockQuotes[0]))
        `when`(stockService.getStockBySymbol(stock.symbol)).thenReturn(Mono.just(stock))
        `when`(investmentAccountRepository.findById(investmentAccount.id!!)).thenReturn(Mono.just(investmentAccount))

        val stockDTO = StockDTO.mapToDto(stock, stockQuotes[0])
        stockService.getStockDetails(stock.symbol, investmentAccount.id!!)
            .doOnNext { result ->
                assert(result.stock == stockDTO)
                assert(result.portfolioEntry == PortfolioEntryDTO.mapToDto(portfolioEntry, stockDTO))
            }
            .subscribe()
    }

    @Test
    fun `test getStockByDescription`() {
        val description = "Apple Inc."
        val stock = Stock(symbol = "AAPL", description = "Apple Inc.", figi = "BBG000B9XRY4", currency = Currency.USD, name = "Apple")

        // Mocken der Rückgabe von `findByDescription`
        `when`(stockRepository.findByDescription(description)).thenReturn(Mono.just(stock))

        // Testaufruf und Überprüfung
        stockService.getStockByDescription(description)
            .doOnNext { result ->
                assert(result.symbol == "AAPL")
                assert(result.description == description)
            }
            .subscribe()
    }


    @Test
    fun `test getStocks`() {
        val stock1 = Stock(symbol = "AAPL", description = "Apple Inc.", figi = "BBG000B9XRY4", currency = Currency.USD, name = "Apple")
        val stock2 = Stock(symbol = "GOOG", description = "Alphabet Inc.", figi = "BBG000B9XRY4", currency = Currency.USD, name = "Google")
        val stocks = listOf(stock1, stock2)

        // Mocken der Rückgabe von `findAll`
        `when`(stockRepository.findAll()).thenReturn(Flux.fromIterable(stocks))

        // Testaufruf und Überprüfung
        stockService.getStocks()
            .collectList()
            .doOnNext { result ->
                assert(result.size == 2)
                assert(result[0].symbol == "AAPL")
                assert(result[1].symbol == "GOOG")
            }
            .subscribe()
    }


    /**
     * Tests the [StockService.getStockBySymbol] method to ensure it retrieves the correct Stock entity by its symbol.
     */
    @Test
    fun `test getStockBySymbol`() {
        stockService.getStockBySymbol("AAPL")
            .doOnNext { result ->
                assert(result.symbol == "AAPL")
            }
            .subscribe()
    }

    /**
     * Tests the [StockService.calculateAveragePrice] method to ensure the average price is calculated
     * correctly over a given time range.
     */
    @Test
    fun `test calculateAveragePrice`() {
        val from = LocalDateTime.now().minusDays(50)
        val to = LocalDateTime.now()

        stockService.calculateAveragePrice("AAPL", from, to)
            .doOnNext { averagePrice ->
                assert(averagePrice.compareTo(BigDecimal("200.00")) == 0)
            }
            .subscribe()
    }

    @Test
    fun `test calculateAveragePrice without time limit`() {

        stockService.calculateAveragePrice("AAPL")
            .doOnNext { averagePrice ->
                assert(averagePrice.compareTo(BigDecimal("200.00")) == 0)
            }
            .subscribe()
    }

    /**
     * Tests the [StockService.getStockHistoryBySymbol] method to ensure all quotes for a stock are retrieved correctly.
     */
    @Test
    fun `test getStockHistoryBySymbol without time limits`() {
        stockService.getStockHistoryBySymbol("AAPL")
            .collectList()
            .doOnNext { stockQuotes ->
                assert(stockQuotes.size == 5)
                assert(stockQuotes[0].currentPrice == BigDecimal(100) || stockQuotes[1].currentPrice == BigDecimal(100))
            }
            .subscribe()
    }

    /**
     * Tests the [StockService.getStockHistoryBySymbol] method with a time filter to ensure only quotes
     * within the specified time range are retrieved.
     */
    @Test
    fun `test getStockHistory with time filter`() {
        val from = LocalDateTime.now().minusDays(2)
        val to = LocalDateTime.now().minusDays(1)

        stockService.getStockHistoryBySymbol("AAPL", from, to)
            .collectList()
            .doOnNext { result ->
                assert(result.size == 1)
                assert(result[0].currentPrice == BigDecimal(100))
            }
            .subscribe()
    }

    @Test
    fun `get the latest quote of a corresponding stock`() {
        `when`(stockQuoteLatestRepository.findById(stock.symbol)).thenReturn(Mono.just(stockQuoteLatest))
        `when`(stockQuoteRepository.findById(stockQuoteLatest.quote_id)).thenReturn(Mono.just(stockQuotes[0]))
        `when`(stockService.getLatestQuoteBySymbol(stock.symbol)).thenReturn(Mono.just(stockQuotes[0]))

        stockService.getLatestQuoteBySymbol(stock.symbol)
            .doOnNext { result ->
                assert(result.currentPrice == BigDecimal(100))
                assert(result.stockSymbol == stock.symbol)
            }
            .subscribe()
    }

}