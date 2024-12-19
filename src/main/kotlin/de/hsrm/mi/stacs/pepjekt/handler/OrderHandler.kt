package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IOrderService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class OrderHandler(private val orderService: IOrderService) {

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