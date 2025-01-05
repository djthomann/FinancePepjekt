package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IOrderService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Handles the HTTP requests related to stock orders, such as placing buy and sell orders,
 * and retrieving orders by investment account.
 *
 * @param orderService The service that performs the actual operations related to stock orders.
 */
@Component
class OrderHandler(private val orderService: IOrderService) {

    /**
     * Handles a request to place a buy order for stock in an investment account.
     *
     * If any required parameter is missing, an error response will be returned. If the order
     * cannot be placed, a 404 Not Found response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the created order or an error response if invalid.
     * @throws IllegalArgumentException If any required parameter (investmentAccountId, stockSymbol,
     *                                  volume, executionTime) is missing.
     */
    fun postBuyStock(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }
        val stockSymbol =
            request.queryParam("stockSymbol").orElseThrow { IllegalArgumentException("stockSymbol is required") }
        val volume =
            BigDecimal(request.queryParam("volume").orElseThrow { IllegalArgumentException("volume is required") })
        val executionTime = LocalDateTime.parse(
            request.queryParam("executionTime").orElseThrow { IllegalArgumentException("executionTime is required") })

        return orderService.placeBuyOrder(investmentAccountId, stockSymbol, volume, executionTime)
            .flatMap { order ->
                ServerResponse.ok().bodyValue(order)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
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
    fun postSellStock(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }
        val stockSymbol =
            request.queryParam("stockSymbol").orElseThrow { IllegalArgumentException("stockSymbol is required") }
        val volume =
            BigDecimal(request.queryParam("volume").orElseThrow { IllegalArgumentException("volume is required") })
        val executionTime = LocalDateTime.parse(
            request.queryParam("executionTime").orElseThrow { IllegalArgumentException("executionTime is required") })

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

        return orderService.getOrdersByInvestmentAccount(investmentAccountId)
            .collectList()
            .flatMap { orders ->
                if (orders.isEmpty()) {
                    ServerResponse.notFound().build()
                } else {
                    ServerResponse.ok().bodyValue(orders)
                }
            }
    }
}