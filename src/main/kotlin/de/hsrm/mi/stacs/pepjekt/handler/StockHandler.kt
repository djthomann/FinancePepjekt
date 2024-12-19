package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class StockHandler(private val stockService: IStockService, private val orderService: IStockService) {

    fun getStockDetailsBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        TODO("Not yet implemented")
    }

    fun getStockDetailsByName(request: ServerRequest): Mono<ServerResponse> {
        val name = request.queryParam("name").orElseThrow { IllegalArgumentException("name is required") }

        TODO("Not yet implemented")
    }

    fun getStockBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        return stockService.getStockBySymbol(symbol)
            .flatMap { stock ->
                ServerResponse.ok().bodyValue(stock)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun getStockByName(request: ServerRequest): Mono<ServerResponse> {
        val name = request.queryParam("name").orElseThrow { IllegalArgumentException("name is required") }

        TODO("Not yet implemented")
    }

    fun getCurrentStockValue(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        TODO("Not yet implemented")
    }

    fun getStockDayLow(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        TODO("Not yet implemented")
    }

    fun getStockDayHigh(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        TODO("Not yet implemented")
    }

    fun getStockHistoryBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }
        val fromParam = request.queryParam("from")
        val toParam = request.queryParam("to")

        return if (fromParam.isPresent && toParam.isPresent) {
            val from = LocalDateTime.parse(fromParam.get())
            val to = LocalDateTime.parse(toParam.get())

            stockService.getStockHistory(symbol, from, to)
                .collectList()
                .flatMap { history ->
                    ServerResponse.ok().bodyValue(history)
                }
                .switchIfEmpty(ServerResponse.notFound().build())
        } else {
            stockService.getStockHistory(symbol)
                .collectList()
                .flatMap { history ->
                    ServerResponse.ok().bodyValue(history)
                }
                .switchIfEmpty(ServerResponse.notFound().build())
        }
    }

    fun getStockHistoryByName(request: ServerRequest): Mono<ServerResponse> {
        val name = request.queryParam("name").orElseThrow { IllegalArgumentException("name is required") }
        val from = LocalDateTime.parse(request.queryParam("from").orElseThrow { IllegalArgumentException("from is required") })
        val to = LocalDateTime.parse(request.queryParam("to").orElseThrow { IllegalArgumentException("to is required") })

        TODO("Not yet implemented")
    }

    fun getStockAveragePrice(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }
        val from = LocalDateTime.parse(request.queryParam("from").orElseThrow { IllegalArgumentException("from is required") })
        val to = LocalDateTime.parse(request.queryParam("to").orElseThrow { IllegalArgumentException("to is required") })

        return stockService.calculateAveragePrice(symbol, from, to)
            .flatMap { history ->
                ServerResponse.ok().bodyValue(history)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }
}
