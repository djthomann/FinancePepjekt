package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.entities.dtos.PortfolioEntryDTO
import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDTO
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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

    private lateinit var stockService: StockService
    private lateinit var stock: Stock
    private lateinit var stock2: Stock
    private lateinit var stocks: List<Stock>
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
        stock2 = Stock(symbol = "GOOG", description = "Alphabet Inc.", figi = "BBG000B9XRY4", currency = Currency.USD, name = "Google")
        stocks = listOf(stock, stock2)

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
        stockQuoteLatest = StockQuoteLatest(stockSymbol = stock.symbol, quoteId = 1L)
        portfolioEntry = PortfolioEntry(investmentAccountId = investmentAccount.id!!, stockSymbol = stock.symbol, id
        = 1L, quantity = 10.0, totalInvestAmount = 1000.0.toBigDecimal())

        stockService = StockService(stockRepository, stockQuoteRepository, stockQuoteLatestRepository, investmentAccountRepository, portfolioEntryRepository)

        `when`(stockRepository.findBySymbol(stock.symbol)).thenReturn(Mono.just(stock))
        `when`(stockRepository.findByDescription(stock.description)).thenReturn(Mono.just(stock))
        `when`(stockRepository.findAll()).thenReturn(Flux.fromIterable(stocks))

        `when`(stockQuoteRepository.findByStockSymbol(stock.symbol)).thenReturn(Flux.fromIterable(stockQuotes))
        `when`(stockQuoteRepository.findById(stockQuoteLatest.quoteId)).thenReturn(Mono.just(stockQuotes[0]))

        `when`(stockQuoteLatestRepository.findById(stock.symbol)).thenReturn(Mono.just(stockQuoteLatest))

        `when`(stockService.getLatestQuoteBySymbol(stock.symbol)).thenReturn(Mono.just(stockQuotes[0]))
        `when`(stockService.getStockBySymbol(stock.symbol)).thenReturn(Mono.just(stock))

        `when`(investmentAccountRepository.findById(investmentAccount.id!!)).thenReturn(Mono.just(investmentAccount))
    }

    /**
     * Tests the [StockService.getStockDetails] method to ensure it retrieves the correct details
     * of a stock and the associated portfolio entry for a given stock symbol and investment account.
     */
    @Test
    fun `test getStockDetails`() {
        val stockDTO = StockDTO.mapToDto(stock, stockQuotes[0])
        stockService.getStockDetails(stock.symbol, investmentAccount.id!!)
            .doOnNext { result ->
                assert(result.stock == stockDTO)
                assert(result.portfolioEntry == PortfolioEntryDTO.mapToDto(portfolioEntry, stockDTO))
            }
            .subscribe()
    }

    /**
     * Tests the [StockService.getStockByDescription] method to verify that it correctly retrieves a stock
     * based on the description provided. This test checks that the stock returned has the expected symbol and description.
     */
    @Test
    fun `test getStockByDescription`() {
        stockService.getStockByDescription(stock.description)
            .doOnNext { result ->
                assert(result.symbol == stock.symbol)
                assert(result.description == stock.description)
            }
            .subscribe()
    }

    /**
     * Tests the [StockService.getStocks] method to ensure that all available stocks are retrieved correctly.
     * This test checks if the list of stocks contains the correct number of stocks and the expected symbols.
     */
    @Test
    fun `test getStocks`() {
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

    /**
     * Tests the [StockService.calculateAveragePrice] method to ensure that the average price is calculated correctly
     * when no specific time limit is applied. This test verifies that the method can compute the average price
     * for all available stock data.
     */
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

    /**
     * Tests the [StockService.getLatestQuoteBySymbol] method to ensure that the latest stock quote for a given stock symbol
     * is correctly retrieved. This test checks if the latest quote contains the expected current price and stock symbol.
     */
    @Test
    fun `get the latest quote of a corresponding stock`() {
        stockService.getLatestQuoteBySymbol(stock.symbol)
            .doOnNext { result ->
                assert(result.currentPrice == BigDecimal(100))
                assert(result.stockSymbol == stock.symbol)
            }
            .subscribe()
    }

}