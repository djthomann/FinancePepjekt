package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
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

        orderService = OrderService(operator, orderRepository, investmentAccountRepository, stockRepository,
            investmentAccountService, latestIStockQuoteRepository, quoteRepository)
    }

    /**
     * Tests the [OrderService.placeBuyOrder] method to ensure buy orders are saved correctly.
     */
    @Test
    fun `test placeBuyOrder`() {
        val investmentAccountId = 1L
        val stockSymbol = "AAPL"
        val volume = BigDecimal(10)
        val executionTime = LocalDateTime.now()

        orderService.placeBuyOrder(investmentAccountId, stockSymbol, volume, executionTime)
            .subscribe {
                verify(orderRepository).save(any())
            }
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