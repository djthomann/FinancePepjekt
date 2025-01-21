package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.dtos.OrderDTO
import de.hsrm.mi.stacs.pepjekt.services.IOrderService
import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Handles the HTTP requests related to stock orders, such as placing buy and sell orders,
 * and retrieving orders by investment account.
 *
 * @param orderService The service that performs the actual operations related to stock orders.
 */
@Component
class OrderHandler(private val orderService: IOrderService, private val stockService: IStockService) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Handles a request to buy stock in an investment account.
     *
     * If any required parameter is missing or invalid, an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the updated investment account or an error response if invalid.
     * @throws IllegalArgumentException If any required parameter (investmentAccountId, stockSymbol, volume) is missing.
     */
    fun placeBuyOrder(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .map { it.toLong() }
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        val stockSymbol = request.queryParam("stockSymbol")
            .orElseThrow { IllegalArgumentException("stockSymbol is required") }

        val purchaseAmount = request.queryParam("purchaseAmount")
            .map { BigDecimal(it) }
            .orElseThrow { IllegalArgumentException("purchaseAmount is required") }

        val executionTime = request.queryParam("executionTime")
            .map { LocalDateTime.parse(it) }
            .orElseThrow { IllegalArgumentException("executionTime is required") }

        logger.info(
            "Place BUY Order to time $executionTime for $purchaseAmount $ of $stockSymbol for investmentAccount"
                    + " $investmentAccountId"
        )
        return orderService.placeBuyOrder(investmentAccountId, stockSymbol, purchaseAmount, executionTime)
            .flatMap { createdOrder ->
                ServerResponse.ok().bodyValue(createdOrder)
            }
    }

    /**
     * Handles a request to place a sell order for stock in an investment account.
     *
     * If any required parameter is missing, an error response will be returned. If the order
     * cannot be placed, a 404 Not Found response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the created order or an error response if invalid.
     * @throws IllegalArgumentException If any required parameter (investmentAccountId, stockSymbol,
     *                                  volume, executionTime) is missing.
     */
    fun placeSellOrder(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .map { it.toLong() }
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }
        val stockSymbol =
            request.queryParam("stockSymbol").orElseThrow { IllegalArgumentException("stockSymbol is required") }
        val volume =
            request.queryParam("volume").orElseThrow { IllegalArgumentException("volume is required") }.toDouble()
        val executionTime = LocalDateTime.parse(
            request.queryParam("executionTime").orElseThrow { IllegalArgumentException("executionTime is required") })

        logger.info(
            "Place SELL Order to time $executionTime for $volume $ of $stockSymbol for investmentAccount"
                    + "$investmentAccountId"
        )

        return orderService.placeSellOrder(investmentAccountId, stockSymbol, volume, executionTime)
            .flatMap { order ->
                ServerResponse.ok().bodyValue(order)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }


    /**
     * Handles a request to retrieve the list of orders for a specific investment account.
     *
     * If no orders are found, a 404 Not Found response will be returned.
     *
     * @param request The incoming server request containing the investment account ID.
     * @return A Mono containing the server response with the list of orders or a 404 if no orders are found.
     * @throws IllegalArgumentException If the investmentAccountId is missing.
     */
    fun getOrders(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .map { it }
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        logger.info("Fetch orders for investmentaccount $investmentAccountId")

        return orderService.getOrdersByInvestmentAccountId(investmentAccountId)
            .collectList()
            .flatMap { orders ->
                val stockMonos = orders.map { order ->
                    stockService.getStockBySymbol(order.stockSymbol)
                        .map { stock ->
                            OrderDTO(
                                id = order.id,
                                purchaseAmount = order.purchaseAmount,
                                type = order.type,
                                executionTimestamp = order.executionTimestamp,
                                stock = stock,
                                investmentAccountId = investmentAccountId.toLong(),
                                stockSymbol = stock.symbol,
                            )
                        }
                }

                Flux.merge(stockMonos)
                    .collectList()
                    .flatMap { orderDTOs ->
                        ServerResponse.ok().bodyValue(orderDTOs)
                    }
            }
    }
}
