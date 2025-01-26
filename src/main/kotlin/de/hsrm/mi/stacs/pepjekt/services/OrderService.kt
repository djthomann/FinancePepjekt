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

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Places a buy order for a specific stock and associates it with an investment account.
     *
     * @param investmentAccountId the ID of the investment account placing the buy order
     * @param stockSymbol the symbol of the stock to purchase
     * @param purchaseAmount the amount of money to spend on the stock purchase
     * @param executionTime the time when the order is executed
     * @return a [Mono] emitting the created [Order], or an error if the operation fails
     */
    override fun placeBuyOrder(
        investmentAccountId: Long,
        stockSymbol: String,
        purchaseAmount: BigDecimal,
        executionTime: LocalDateTime
    ): Mono<Order> {
        logger.info("Placing buy order for stock: {} with amount: {}", stockSymbol, purchaseAmount)

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
                logger.error("Current price for stock {} is zero, unable to calculate volume", stockSymbol)
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

            logger.info("Created buy order: {}", order)
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
        logger.info("Placing sell order for stock: {} with volume: {}", stockSymbol, volume)

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

            logger.info("Created sell order: {}", order)
            orderRepository.save(order).`as`(operator::transactional)
                .doOnError { error -> logger.error("Error placing sell order for stock: {}", stockSymbol, error) }
        }
    }

    /**
     * Retrieves all orders associated with a specific investment account.
     *
     * @param investmentAccountId the ID of the investment account whose orders are to be retrieved
     * @return a [Flux] emitting the [Order] instances associated with the given investment account
     */
    override fun getOrdersByInvestmentAccountId(investmentAccountId: String): Flux<Order> {
        logger.debug("Fetching orders for investment account ID: {}", investmentAccountId)
        return orderRepository.findByInvestmentAccountId(investmentAccountId.toLong())
            .doOnError { error -> logger.error("Error fetching orders for investment account ID: {}", investmentAccountId, error) }
    }

    /**
     * Processes pending orders and executes them based on their execution time.
     * For buy orders, invokes the buyStock method to complete the purchase.
     */
    override fun processOrders(): Mono<Void> {
        logger.debug("Starting to process Orders")

        val now = LocalDateTime.now()

        return orderRepository.findAll()
            .filter { order -> order.executionTimestamp.isBefore(now) || order.executionTimestamp.isEqual(now) }
            .flatMap { order ->
                when (order.type) {
                    OrderType.BUY -> {
                        logger.info("Processing BUY order for stock: {}", order.stockSymbol)
                        investmentAccountService.buyStock(
                            order.investmentAccountId,
                            order.stockSymbol,
                            order.purchaseAmount
                        )
                            .then(Mono.just(order))
                    }

                    OrderType.SELL -> {
                        logger.info("Processing SELL order for stock: {}", order.stockSymbol)
                        investmentAccountService.sellStock(
                            order.investmentAccountId,
                            order.stockSymbol,
                            order.purchaseVolume
                        )
                            .then(Mono.just(order))
                    }
                }
            }
            .flatMap { order ->
                logger.info("Deleting order for stock: {} after processing.", order.stockSymbol)
                orderRepository.delete(order)
            }.onErrorContinue { error, _ ->
                logger.error("Error during order processing: {}", error.message, error)
            }
            .then()
            .doOnTerminate { logger.debug("Finished processing all orders.") }
    }
}
