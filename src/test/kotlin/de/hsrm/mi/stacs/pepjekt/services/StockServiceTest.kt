package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.repositories.IStockQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
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
    private val quoteRepository: IStockQuoteRepository = mock(IStockQuoteRepository::class.java)

    private lateinit var stockService: StockService

    /**
     * Sets up test environment by initializing mocked repositories, defining their behaviors,
     * and preparing test data for the Stock and Quote entities.
     */
    @BeforeEach
    fun setUp() {
        val stock = Stock(symbol = "AAPL", description = "Apple Inc.", figi = "BBG000B9XRY4", currency = Currency.USD)
        val stockQuotes = listOf(
            StockQuote(value = BigDecimal(100), timeStamp = LocalDateTime.now().minusDays(1), stock = stock),
            StockQuote(value = BigDecimal(100), timeStamp = LocalDateTime.now().minusDays(21), stock = stock),
            StockQuote(value = BigDecimal(150), timeStamp = LocalDateTime.now().minusDays(5), stock = stock),
            StockQuote(value = BigDecimal(300), timeStamp = LocalDateTime.now().minusHours(1), stock = stock),
            StockQuote(value = BigDecimal(350), timeStamp = LocalDateTime.now().minusHours(15), stock = stock)
        )

        stockService = StockService(stockRepository, quoteRepository)
        `when`(stockRepository.findBySymbol("AAPL")).thenReturn(Mono.just(stock))
        `when`(quoteRepository.findByStock(stock)).thenReturn(Flux.fromIterable(stockQuotes))
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
     * Tests the [StockService.getStockHistoryBySymbol] method to ensure all quotes for a stock are retrieved correctly.
     */
    @Test
    fun `test getStockHistory`() {
        stockService.getStockHistoryBySymbol("AAPL")
            .collectList()
            .doOnNext { result ->
                assert(result.size == 5)
                assert(result[0].value == BigDecimal(100) || result[1].value == BigDecimal(100))
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
                assert(result[0].value == BigDecimal(100))
            }
            .subscribe()
    }
}