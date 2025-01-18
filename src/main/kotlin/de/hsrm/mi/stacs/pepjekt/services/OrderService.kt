package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Order
import de.hsrm.mi.stacs.pepjekt.entities.OrderType
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IOrderRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Service for managing stock orders, including placing buy and sell orders and retrieving
 * orders for a specific investment account.
 *
 * This service interacts with investment account, stock, and order repositories while
 * ensuring transactional consistency for all operations.
 */
@Service
class OrderService(
    val operator: TransactionalOperator,
    val orderRepository: IOrderRepository,
    val investmentAccountRepository: IInvestmentAccountRepository,
    val stockRepository: IStockRepository,
    val investmentAccountService: IInvestmentAccountService,
) : IOrderService {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Places a buy order for a specific stock and associates it with an investment account.
     *
     * @param investmentAccountId the ID of the investment account placing the buy order
     * @param stockSymbol the symbol of the stock to purchase
     * @param volume the volume of the stock to purchase
     * @param executionTime the time when the order is executed
     * @return a [Mono] emitting the created [Order], or an error if the operation fails
     */
    override fun placeBuyOrder(
        investmentAccountId: Long,
        stockSymbol: String,
        purchaseAmount: BigDecimal,
        executionTime: LocalDateTime
    ): Mono<Order> {
        return Mono.zip(
            investmentAccountRepository.findById(investmentAccountId),
            stockRepository.findBySymbol(stockSymbol)
        ).flatMap { tuple ->
            val (account, stock) = tuple
            val order = Order(
                purchaseAmount = purchaseAmount,
                type = OrderType.BUY,
                investmentAccountId = account.id!!,
                stockSymbol = stock.symbol,
                executionTimestamp = executionTime
            )
            orderRepository.save(order).`as`(operator::transactional)
        }
    }

    /**
     * Places a sell order for a specific stock and associates it with an investment account.
     *
     * @param investmentAccountId the ID of the investment account placing the sell order
     * @param stockSymbol the symbol of the stock to sell
     * @param volume the volume of the stock to sell
     * @param executionTime the time when the order is executed
     * @return a [Mono] emitting the created [Order], or an error if the operation fails
     */
    override fun placeSellOrder(
        investmentAccountId: String,
        stockSymbol: String,
        volume: BigDecimal,
        executionTime: LocalDateTime
    ): Mono<Order> {
        return Mono.zip(
            investmentAccountRepository.findById(investmentAccountId.toLong()),
            stockRepository.findBySymbol(stockSymbol)
        ).flatMap { tuple ->
            val (account, stock) = tuple
            val order = Order(
                type = OrderType.SELL,
                investmentAccountId = account.id!!,
                stockSymbol = stock.symbol,
                id = TODO(),
                purchaseAmount = TODO(),
                executionTimestamp = TODO()
            )
            orderRepository.save(order).`as`(operator::transactional)
        }
    }


    /**
     * Retrieves all orders associated with a specific investment account.
     *
     * @param investmentAccountId the ID of the investment account whose orders are to be retrieved
     * @return a [Flux] emitting the [Order] instances associated with the given investment account
     */
    override fun getOrdersByInvestmentAccountId(investmentAccountId: String): Flux<Order> {
        return orderRepository.findByInvestmentAccountId(investmentAccountId.toLong())
    }

    /**
     * Processes pending orders and executes them based on their execution time.
     * For buy orders, invokes the buyStock method to complete the purchase.
     */
    override fun processOrders(): Mono<Void> {
        log.info("Starting to process Orders")

        val now = LocalDateTime.now()

        return orderRepository.findAll()
            .doOnTerminate { log.info("Completed order fetching.") }
            .filter { order -> order.executionTimestamp.isBefore(now) || order.executionTimestamp.isEqual(now) }
            .flatMap { order ->
                when (order.type) {
                    OrderType.BUY -> {
                        log.info("Order ${order.stockSymbol} processed")
                        investmentAccountService.buyStock(order.investmentAccountId, order.stockSymbol, order.purchaseAmount)
                            .then(Mono.just(order))
                            .doOnTerminate { log.info("Completed BUY order processing for ${order.stockSymbol}") }
                    }
                    OrderType.SELL -> {
                        // TODO handle SELL orders if necessary
                        log.warn("SELL order processing not implemented.")
                        Mono.empty<Order>()
                    }
                }
            }
            .flatMap { order ->
                // Mark the order as processed, or delete it if completed
                log.info("Deleting order ${order.stockSymbol} after processing.")
                orderRepository.delete(order)
            }
            .then()
            .doOnTerminate { log.info("Finished processing all orders.") }
            .doOnError { error -> log.error("Error during order processing: ${error.message}", error) }
    }


}
