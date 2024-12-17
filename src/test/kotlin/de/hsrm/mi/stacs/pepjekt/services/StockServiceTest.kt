package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.repositories.IQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test

class StockServiceTest {

    private val stockRepository: IStockRepository = mock(IStockRepository::class.java)
    private val quoteRepository: IQuoteRepository = mock(IQuoteRepository::class.java)

    private lateinit var stockService: StockService

    @BeforeEach
    fun setUp() {
        val stock = Stock(symbol = "AAPL", description = "Apple Inc.", figi = "BBG000B9XRY4", currency = Currency.USD)
        val quotes = listOf(
            Quote(value = BigDecimal(100), timeStamp = LocalDateTime.now().minusDays(1), stock = stock),
            Quote(value = BigDecimal(100), timeStamp = LocalDateTime.now().minusDays(21), stock = stock),
            Quote(value = BigDecimal(150), timeStamp = LocalDateTime.now().minusDays(5), stock = stock),
            Quote(value = BigDecimal(300), timeStamp = LocalDateTime.now().minusHours(1), stock = stock),
            Quote(value = BigDecimal(350), timeStamp = LocalDateTime.now().minusHours(15), stock = stock)
        )

        stockService = StockService(stockRepository, quoteRepository)
        `when`(stockRepository.findBySymbol("AAPL")).thenReturn(Mono.just(stock))
        `when`(quoteRepository.findByStock(stock)).thenReturn(Flux.fromIterable(quotes))
    }

    @Test
    fun `test getStockBySymbol`() {
        stockService.getStockBySymbol("AAPL")
            .doOnNext { result ->
                assert(result.symbol == "AAPL")
            }
            .subscribe()
    }

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
    fun `test getStockHistory`() {
        stockService.getStockHistory("AAPL")
            .collectList()
            .doOnNext { result ->
                assert(result.size == 5)
                assert(result[0].value == BigDecimal(100) || result[1].value == BigDecimal(100))
            }
            .subscribe()
    }

    @Test
    fun `test getStockHistory with time filter`() {
        val from = LocalDateTime.now().minusDays(2)
        val to = LocalDateTime.now().minusDays(1)

        stockService.getStockHistory("AAPL", from, to)
            .collectList()
            .doOnNext { result ->
                assert(result.size == 1)
                assert(result[0].value == BigDecimal(100))
            }
            .subscribe()
    }

}