package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * Handles HTTP requests related to stock information, including retrieving stock details,
 * stock history, and calculating average stock prices.
 *
 * @param stockService The service responsible for interacting with stock data, such as fetching stock details,
 *                     stock history, and calculating average prices.
 * @param orderService The service for interacting with stock orders (this is injected but currently not used).
 */
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


    /**
     * Handles a request to retrieve stock data by its symbol.
     *
     * Extracts the stock symbol from the request's query parameters and retrieves the stock data for the given symbol.
     *
     * @param request The incoming server request containing the stock symbol.
     * @return A Mono containing the server response with the stock data or a 404 Not Found if the stock is not found.
     * @throws IllegalArgumentException If the stock symbol is not provided in the request.
     */
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

    /**
     * Handles a request to retrieve the history of a stock by its symbol within a specified time range.
     *
     * Extracts the stock symbol and the optional 'from' and 'to' query parameters from the request.
     * If the 'from' and 'to' parameters are provided, the stock history is fetched for the given time range.
     * If only the symbol is provided, the stock history is fetched without time filtering.
     *
     * @param request The incoming server request containing the stock symbol and optional time range parameters.
     * @return A Mono containing the server response with the stock history or a 404 Not Found if no history is found.
     * @throws IllegalArgumentException If the stock symbol is not provided in the request.
     */
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

    /**
     * Handles a request to calculate the average stock price within a specified time range by its symbol.
     *
     * Extracts the stock symbol and the 'from' and 'to' time range query parameters from the request,
     * and retrieves the average price for the given symbol within the specified range.
     *
     * @param request The incoming server request containing the stock symbol and time range parameters.
     * @return A Mono containing the server response with the calculated average price or a 404 Not Found if no price is found.
     * @throws IllegalArgumentException If the stock symbol or time range parameters are missing.
     */
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
