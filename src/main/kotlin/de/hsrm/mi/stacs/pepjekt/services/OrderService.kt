package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Order
import de.hsrm.mi.stacs.pepjekt.entities.OrderType
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import java.math.BigDecimal
import java.math.RoundingMode
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
    val latestIStockQuoteRepository: IStockQuoteLatestRepository,
    val quoteRepository: IStockQuoteRepository
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
            stockRepository.findBySymbol(stockSymbol),
            latestIStockQuoteRepository.findById(stockSymbol)
                .flatMap { latestStockQuote ->
                    quoteRepository.findById(latestStockQuote.quoteId)
                }
        ).flatMap { tuple ->
            val (account, stock, quote) = tuple
            val calculatedVolume = if (quote.currentPrice != BigDecimal.ZERO) {
                purchaseAmount.divide(quote.currentPrice, 10, RoundingMode.UP)
            } else {
                return@flatMap Mono.error(IllegalArgumentException("Current price cannot be null or zero"))
            }

            val order = Order(
                purchaseAmount = purchaseAmount,
                purchaseVolume = calculatedVolume.toDouble(),
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
        investmentAccountId: Long,
        stockSymbol: String,
        volume: Double,
        executionTime: LocalDateTime
    ): Mono<Order> {
        return Mono.zip(
            investmentAccountRepository.findById(investmentAccountId),
            stockRepository.findBySymbol(stockSymbol),
            latestIStockQuoteRepository.findById(stockSymbol)
                .flatMap { latestStockQuote ->
                    quoteRepository.findById(latestStockQuote.quoteId)
                }
        ).flatMap { tuple ->
            val (account, stock, quote) = tuple
            val calculatedAmount = quote.currentPrice.multiply(volume.toBigDecimal())

            val order = Order(
                type = OrderType.SELL,
                purchaseAmount = calculatedAmount,
                purchaseVolume = volume,
                executionTimestamp = executionTime,
                investmentAccountId = account.id!!,
                stockSymbol = stock.symbol,
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
                        log.info("BUY Order ${order.stockSymbol} processed")
                        investmentAccountService.buyStock(
                            order.investmentAccountId,
                            order.stockSymbol,
                            order.purchaseAmount
                        )
                            .then(Mono.just(order))
                            .doOnTerminate { log.info("Completed BUY order processing for ${order.stockSymbol}") }
                    }

                    OrderType.SELL -> {
                        log.info("SELL Order ${order.stockSymbol} processed")
                        investmentAccountService.sellStock(
                            order.investmentAccountId,
                            order.stockSymbol,
                            order.purchaseVolume
                        )
                            .then(Mono.just(order))
                            .doOnTerminate { log.info("Completed SELL order processing for ${order.stockSymbol}") }
                    }
                }
            }
            .flatMap { order ->
                // Mark the order as processed, or delete it if completed
                log.info("Deleting order ${order.stockSymbol} after processing.")
                orderRepository.delete(order)
            }.onErrorContinue { error, _ ->
                log.error("Error during order processing: ${error.message}", error)
            }
            .then()
            .doOnTerminate { log.info("Finished processing all orders.") }
    }


}
