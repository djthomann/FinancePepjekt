package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IOrderRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
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

    private lateinit var orderService: OrderService

    /**
     * Sets up the test environment by initializing mock dependencies and defining their behaviors.
     */
    @BeforeEach
    fun setUp() {
        val investmentAccountId = "1"
        val stockSymbol = "AAPL"
        val volume = BigDecimal(10)
        val stock =
            Stock(symbol = stockSymbol, description = "Apple Inc.", figi = "BBG000B9XRY4", currency = Currency.USD)

        val ordersList = listOf(
            Order(volume = 10f, type = OrderType.BUY, investmentAccount = InvestmentAccount(id = 1L), stock = stock),
            Order(volume = 5f, type = OrderType.SELL, investmentAccount = InvestmentAccount(id = 1L), stock = stock)
        )

        val investmentAccount = InvestmentAccount(id = 1L)

        `when`(investmentAccountRepository.findById(investmentAccountId.toLong())).thenReturn(
            Mono.just(
                investmentAccount
            )
        )
        `when`(stockRepository.findBySymbol(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(orderRepository.save(any())).thenReturn(
            Mono.just(
                Order(
                    volume = volume.toFloat(),
                    type = OrderType.BUY,
                    investmentAccount = investmentAccount,
                    stock = stock
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
                    volume = volume.toFloat(),
                    type = OrderType.SELL,
                    investmentAccount = investmentAccount,
                    stock = stock
                )
            )
        )

        orderService = OrderService(operator, orderRepository, investmentAccountRepository, stockRepository)
    }

    /**
     * Tests the [OrderService.placeBuyOrder] method to ensure buy orders are saved correctly.
     */
    @Test
    fun `test placeBuyOrder`() {
        val investmentAccountId = "1"
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
        val investmentAccountId = "1"
        val stockSymbol = "AAPL"
        val volume = BigDecimal(10)
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

        orderService.getOrdersByInvestmentAccount(investmentAccountId)
            .collectList()
            .doOnNext { result ->
                assert(result.size == 2)
                assert(result[0].type == OrderType.BUY)
                assert(result[1].type == OrderType.SELL)
            }
            .subscribe()
    }
}