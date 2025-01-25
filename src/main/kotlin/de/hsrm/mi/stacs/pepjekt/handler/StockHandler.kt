package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.dtos.QuoteDTO
import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDTO
import de.hsrm.mi.stacs.pepjekt.services.IFavoriteService
import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime

/**
 * Handles HTTP requests related to stock information, including retrieving stock details,
 * stock history, and calculating average stock prices.
 *
 * @param stockService The service responsible for interacting with stock data, such as fetching stock details,
 *                     stock history, and calculating average prices.
 * @param favoriteService The service for interacting with favorite stocks
 */
@Component
class StockHandler(private val stockService: IStockService, private val favoriteService: IFavoriteService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Handles a request to retrieve all stocks from the database.
     *
     * @param request The incoming server request.
     * @return all stocks in the database
     */
    fun getStocks(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        logger.info("Getting stocks for investment account with id $investmentAccountId")

        return stockService.getAllStocks()
            .flatMap { stock ->
                Mono.zip(
                    stockService.getLatestQuoteBySymbol(stock.symbol),
                    favoriteService.isFavorite(investmentAccountId.toLong(), stock.symbol)
                ).map { tuple ->
                    StockDTO.mapToDto(stock, tuple.t1, tuple.t2)
                }
            }
            .collectList()
            .flatMap { stockDtos ->
                if (stockDtos.isNotEmpty()) {
                    ServerResponse.ok().bodyValue(stockDtos)
                } else {
                    ServerResponse.notFound().build()
                }
            }
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }
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
    fun getStockDetailsBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }
        val investmentAccountId =
            request.queryParam("investmentAccountId").orElseThrow { IllegalArgumentException("investmentAccountId") }

        logger.info("Getting stock details for investment account with id $investmentAccountId and stock with symbol $symbol")

        return stockService.getStockDetails(symbol, investmentAccountId.toLong())
            .flatMap {
                ServerResponse.ok().bodyValue(it)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }
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
        val symbol = request.queryParam("symbol")
            .orElseThrow { IllegalArgumentException("symbol is required") }
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("symbol is required") }

        logger.info("Getting stock by symbol $symbol and investmentAccountId $investmentAccountId")

        return stockService.getStockBySymbol(symbol)
            .flatMap { stock ->
                stockService.getLatestQuoteBySymbol(symbol)
                    .publishOn(Schedulers.boundedElastic())
                    .map { quote ->
                        StockDTO.mapToDto(stock, quote, favoriteService.isFavorite(investmentAccountId.toLong(), stock.symbol).block())
                    }
                    .flatMap { stockDto ->
                        ServerResponse.ok().bodyValue(stockDto)
                    }
            }
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }
    }

    /**
     * Handles a request to retrieve the latest stock value by its symbol.
     *
     * Extracts the stock symbol from the request's query parameters and retrieves the latest stock quote for the specified symbol.
     * Returns a 404 Not Found response if no stock data is available for the provided symbol.
     *
     * @param request The incoming server request containing the stock symbol as a query parameter.
     * @return A Mono containing the server response with the latest stock value, or a 404 Not Found if the stock is not found.
     * @throws IllegalArgumentException If the stock symbol is not provided in the request.
     */
    fun getCurrentStockValue(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        logger.info("Getting current stock value for stock with symbol $symbol")

        return stockService.getLatestQuoteBySymbol(symbol)
            .flatMap {
                ServerResponse.ok().bodyValue(QuoteDTO.mapToDto(it))
            }
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }
    }

    /**
     * Handles a request to retrieve the lowest stock value of the day for a given symbol.
     *
     * Extracts the stock symbol from the request's query parameters and retrieves the lowest stock value of the day
     * for the specified symbol as of the current date and time.
     * Returns a 404 Not Found response if no data is available for the provided symbol.
     *
     * @param request The incoming server request containing the stock symbol as a query parameter.
     * @return A Mono containing the server response with the day's lowest stock value, or a 404 Not Found if no data is found.
     * @throws IllegalArgumentException If the stock symbol is not provided in the request.
     */
    fun getStockDayLow(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        logger.info("Getting stock day low for stock with symbol $symbol")

        return stockService.getDayLow(symbol, LocalDateTime.now())
            .flatMap {
                ServerResponse.ok().bodyValue(QuoteDTO.mapToDto(it))
            }
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }
    }

    /**
     * Handles a request to retrieve the highest stock value of the day for a given symbol.
     *
     * Extracts the stock symbol from the request's query parameters and retrieves the highest stock value of the day
     * for the specified symbol as of the current date and time.
     * Returns a 404 Not Found response if no data is available for the provided symbol.
     *
     * @param request The incoming server request containing the stock symbol as a query parameter.
     * @return A Mono containing the server response with the day's highest stock value, or a 404 Not Found if no data is found.
     * @throws IllegalArgumentException If the stock symbol is not provided in the request.
     */
    fun getStockDayHigh(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        logger.info("Getting stock day high for stock with symbol $symbol")

        return stockService.getDayHigh(symbol, LocalDateTime.now())
            .flatMap {
                ServerResponse.ok().bodyValue(QuoteDTO.mapToDto(it))
            }
            .switchIfEmpty(ServerResponse.notFound().build())
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }
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

        logger.info("Getting stock history by symbol $symbol, from $fromParam to $toParam")

        return if (fromParam.isPresent && toParam.isPresent) {
            val from = LocalDateTime.parse(fromParam.get())
            val to = LocalDateTime.parse(toParam.get())

            stockService.getStockHistoryBySymbol(symbol, from, to)
                .collectList()
                .flatMap { history ->
                    val historyDtos = history.map { QuoteDTO.mapToDto(it) }
                    ServerResponse.ok().bodyValue(historyDtos)
                }
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume { e ->
                    ServerResponse.badRequest().bodyValue("Error: ${e.message}")
                }
        } else {
            stockService.getStockHistoryBySymbol(symbol)
                .collectList()
                .flatMap { history ->
                    val historyDtos = history.map { QuoteDTO.mapToDto(it) }
                    ServerResponse.ok().bodyValue(historyDtos)
                }
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume { e ->
                    ServerResponse.badRequest().bodyValue("Error: ${e.message}")
                }
        }
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
        val fromParam = request.queryParam("from")
        val toParam = request.queryParam("to")

        logger.info("Getting stock average price for symbol $symbol, from $fromParam to $toParam")

        return if (fromParam.isPresent && toParam.isPresent) {
            val from = LocalDateTime.parse(fromParam.get())
            val to = LocalDateTime.parse(toParam.get())

            stockService.calculateAveragePrice(symbol, from, to)
                .flatMap { averagePrice ->
                    ServerResponse.ok().bodyValue(averagePrice)
                }
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume { e ->
                    ServerResponse.badRequest().bodyValue("Error: ${e.message}")
                }
        } else {
            stockService.calculateAveragePrice(symbol)
                .flatMap { averagePrice ->
                    ServerResponse.ok().bodyValue(averagePrice)
                }
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume { e ->
                    ServerResponse.badRequest().bodyValue("Error: ${e.message}")
                }
        }
    }

}
