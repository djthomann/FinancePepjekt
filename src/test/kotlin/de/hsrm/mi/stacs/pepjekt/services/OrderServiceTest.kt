package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import kotlin.test.Test

/**
 * Unit tests for the [OrderService] class.
 *
 * This class ensures the proper functioning of `OrderService` methods through isolated unit tests
 * with mocked dependencies for repositories and transaction operator.
 */
class OrderServiceTest {

    private val orderRepository: IOrderRepository = mock(IOrderRepository::class.java)
    private val investmentAccountRepository: IInvestmentAccountRepository =
        mock(IInvestmentAccountRepository::class.java)
    private val stockRepository: IStockRepository = mock(IStockRepository::class.java)
    private val operator: TransactionalOperator = mock(TransactionalOperator::class.java)
    private val investmentAccountService: IInvestmentAccountService = mock(IInvestmentAccountService::class.java)
    private val latestIStockQuoteRepository: IStockQuoteLatestRepository = mock(IStockQuoteLatestRepository::class.java)
    private val quoteRepository: IStockQuoteRepository = mock(IStockQuoteRepository::class.java)

    private lateinit var orderService: OrderService

    /**
     * Sets up the test environment by initializing mock dependencies and defining their behaviors.
     */
    @BeforeEach
    fun setUp() {
        val investmentAccountId = "1"
        val stockSymbol = "AAPL"
        val volume = BigDecimal(10)
        val stock = Stock(
            symbol = stockSymbol,
            description = "Apple Inc.",
            figi = "BBG000B9XRY4",
            currency = Currency.USD,
            name = "Apple"
        )

        val ordersList = listOf(
            Order(
                purchaseAmount = BigDecimal(10), type = OrderType.BUY, investmentAccountId = 1L, stockSymbol =
                stock.symbol, executionTimestamp = LocalDateTime.now(), purchaseVolume = 10.0
            ),
            Order(
                purchaseAmount = BigDecimal(5), type = OrderType.SELL, investmentAccountId = 1L, stockSymbol =
                stock.symbol, executionTimestamp = LocalDateTime.now(), purchaseVolume = 10.0
            ),
        )

        val investmentAccount = InvestmentAccount(id = 1L, bankAccountId = 1L, ownerId = 1L)

        `when`(investmentAccountRepository.findById(investmentAccountId.toLong())).thenReturn(
            Mono.just(
                investmentAccount
            )
        )
        `when`(stockRepository.findBySymbol(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(orderRepository.save(any())).thenReturn(
            Mono.just(
                Order(
                    type = OrderType.BUY,
                    investmentAccountId = investmentAccount.id!!,
                    stockSymbol = stock.symbol,
                    purchaseAmount = BigDecimal(10),
                    purchaseVolume = 10.0,
                    executionTimestamp = LocalDateTime.now()
                )
            )
        )
        `when`(orderRepository.findByInvestmentAccountId(investmentAccountId.toLong())).thenReturn(
            Flux.fromIterable(
                ordersList
            )
        )
        `when`(orderRepository.save(any())).thenReturn(
            Mono.just(
                Order(
                    type = OrderType.SELL,
                    investmentAccountId = investmentAccount.id!!,
                    stockSymbol = stock.symbol,
                    purchaseAmount = BigDecimal(10),
                    purchaseVolume = 10.0,
                    executionTimestamp = LocalDateTime.now()
                )
            )
        )

        orderService = OrderService(
            operator, orderRepository, investmentAccountRepository, stockRepository,
            investmentAccountService, latestIStockQuoteRepository, quoteRepository
        )
    }

    /**
     * Tests the [OrderService.placeBuyOrder] method to ensure buy orders are saved correctly.
     */
    @Test
    fun `test placeBuyOrder successfully saves order`() {
        val investmentAccountId = 1L
        val stockSymbol = "AAPL"
        val purchaseAmount = BigDecimal(10)
        val executionTime = LocalDateTime.now()

        val account = InvestmentAccount(id = investmentAccountId)
        val stock = Stock(
            symbol = stockSymbol,
            name = "Apple Inc.",
            description = "Apple Stock",
            figi = "BBG000B9XRY4",
            currency = Currency.USD
        )
        val quote = StockQuote(
            id = 1L, currentPrice = BigDecimal(100), change = 0f, percentChange = 0f,
            highPriceOfTheDay = BigDecimal(110), lowPriceOfTheDay = BigDecimal(90),
            openPriceOfTheDay = BigDecimal(100), previousClosePrice = BigDecimal(99),
            timeStamp = executionTime, stockSymbol = stockSymbol
        )

        `when`(investmentAccountRepository.findById(investmentAccountId)).thenReturn(Mono.just(account))
        `when`(stockRepository.findBySymbol(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(latestIStockQuoteRepository.findById(stockSymbol)).thenReturn(
            Mono.just(
                StockQuoteLatest(
                    stockSymbol,
                    1L
                )
            )
        )
        `when`(quoteRepository.findById(1L)).thenReturn(Mono.just(quote))
        `when`(orderRepository.save(any())).thenReturn(
            Mono.just(
                Order(
                    id = 1L, purchaseAmount = purchaseAmount,
                    purchaseVolume = 0.1, type = OrderType.BUY, investmentAccountId = investmentAccountId,
                    stockSymbol = stockSymbol, executionTimestamp = executionTime
                )
            )
        )

        orderService.placeBuyOrder(investmentAccountId, stockSymbol, purchaseAmount, executionTime)
            .subscribe { savedOrder ->
                verify(orderRepository).save(any())
                assertEquals(
                    purchaseAmount.divide(quote.currentPrice, 10, RoundingMode.UP),
                    BigDecimal(savedOrder.purchaseVolume)
                )
            }
    }

    /**
     * Tests the [OrderService.placeBuyOrder] method to ensure buy orders are saved correctly.
     */
    @Test
    fun `test placeBuyOrder with zero currentPrice should throw error`() {
        // Given
        val investmentAccountId = 1L
        val stockSymbol = "AAPL"
        val purchaseAmount = BigDecimal(10)
        val executionTime = LocalDateTime.now()

        val account = InvestmentAccount(id = investmentAccountId)
        val stock = Stock(
            symbol = stockSymbol,
            name = "Apple Inc.",
            description = "Apple Stock",
            figi = "BBG000B9XRY4",
            currency = Currency.USD
        )
        val quote = StockQuote(
            id = 1L, currentPrice = BigDecimal.ZERO, change = 0f, percentChange = 0f,
            highPriceOfTheDay = BigDecimal(110), lowPriceOfTheDay = BigDecimal(90),
            openPriceOfTheDay = BigDecimal(100), previousClosePrice = BigDecimal(99),
            timeStamp = executionTime, stockSymbol = stockSymbol
        )

        // Mocking the repositories to return values
        `when`(investmentAccountRepository.findById(investmentAccountId)).thenReturn(Mono.just(account))
        `when`(stockRepository.findBySymbol(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(latestIStockQuoteRepository.findById(stockSymbol)).thenReturn(
            Mono.just(
                StockQuoteLatest(
                    stockSymbol,
                    1L
                )
            )
        )
        `when`(quoteRepository.findById(1L)).thenReturn(Mono.just(quote))

        orderService.placeBuyOrder(investmentAccountId, stockSymbol, purchaseAmount, executionTime)
            .doOnError { error ->
                assert(error is IllegalArgumentException)
                assertEquals("Current price cannot be null or zero", error.message)
            }
            .subscribe()
    }

    /**
     * Tests the [OrderService.placeSellOrder] method to ensure sell orders are saved correctly.
     */
    @Test
    fun `test placeSellOrder`() {
        val investmentAccountId = 1L
        val stockSymbol = "AAPL"
        val volume = 10.0
        val executionTime = LocalDateTime.now()

        val subscribe = orderService.placeSellOrder(investmentAccountId, stockSymbol, volume, executionTime)
            .subscribe {
                verify(orderRepository).save(any())
            }
    }

    /**
     * Tests the [OrderService.getOrdersByInvestmentAccount] method to ensure all orders for an investment account
     * are retrieved correctly.
     */
    @Test
    fun `test getOrdersByInvestmentAccount`() {
        val investmentAccountId = "1"

        orderService.getOrdersByInvestmentAccountId(investmentAccountId)
            .collectList()
            .doOnNext { result ->
                assert(result.size == 2)
                assert(result[0].type == OrderType.BUY)
                assert(result[1].type == OrderType.SELL)
            }
            .subscribe()
    }
}