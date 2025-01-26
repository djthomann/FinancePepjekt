package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDTO
import de.hsrm.mi.stacs.pepjekt.services.IFavoriteService
import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Handler class for managing favorite stocks.
 * Provides functionality to retrieve, add, and delete favorites associated with an investment account.
 *
 * @property favoriteService Service handling favorite stock operations.
 * @property stockService Service handling stock-related operations.
 */
@Component
class FavoriteHandler(
    private val favoriteService: IFavoriteService,
    private val stockService: IStockService
) {

    private val logger = LoggerFactory.getLogger(FavoriteHandler::class.java)

    /**
     * Retrieves the favorite stocks associated with a specific investment account.
     *
     * @param request The [ServerRequest] containing the query parameter `investmentAccountId`.
     * @return A [Mono] emitting the server response with a list of favorite stocks or an error response.
     * @throws IllegalArgumentException if `investmentAccountId` is missing in the request.
     */
    fun getFavorites(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        logger.info("Getting favorite stocks for investment account with id $investmentAccountId")

        return favoriteService.getFavoritesOfInvestmentAccount(investmentAccountId.toLong())
            .flatMap { favorites -> stockService.getStockBySymbol(favorites.stockSymbol) }
            .collectList()
            .flatMap { stocks ->
                if (stocks.isEmpty()) {
                    logger.info("No favorite stocks found for investment account with id $investmentAccountId")
                    ServerResponse.ok().bodyValue(emptyList<StockDTO>())
                } else {
                    Flux.fromIterable(stocks)
                        .flatMap { stock ->
                            stockService.getLatestQuoteBySymbol(stock.symbol)
                                .map { quote -> StockDTO.mapToDto(stock, quote, true) }
                        }
                        .collectList()
                        .flatMap { stockDtos ->
                            logger.info("Retrieved favorite stocks for investment account with id $investmentAccountId")
                            ServerResponse.ok().bodyValue(stockDtos)
                        }
                }
            }
            .doOnError { error -> logger.error("Error retrieving favorite stocks for account $investmentAccountId", error) }
    }

    /**
     * Adds a stock to the favorites of a specific investment account.
     *
     * @param request The [ServerRequest] containing the query parameters `investmentAccountId` and `stockSymbol`.
     * @return A [Mono] emitting the server response indicating success or error.
     * @throws IllegalArgumentException if `investmentAccountId` or `stockSymbol` is missing in the request.
     */
    fun addFavorites(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }
        val stockSymbol = request.queryParam("stockSymbol")
            .orElseThrow { IllegalArgumentException("stockSymbol is required") }

        logger.info("Adding a favorite stock for investment account with id $investmentAccountId and stock symbol $stockSymbol")

        return favoriteService.addFavoriteToInvestmentAccount(investmentAccountId.toLong(), stockSymbol)
            .then(ServerResponse.ok().build())
            .onErrorResume { error ->
                when (error) {
                    is NoSuchElementException -> {
                        logger.warn("Stock symbol $stockSymbol not found for account $investmentAccountId")
                        ServerResponse.notFound().build()
                    }
                    else -> {
                        logger.error("Error adding favorite stock for account $investmentAccountId", error)
                        ServerResponse.badRequest().bodyValue(error.message ?: "Unknown error")
                    }
                }
            }
    }

    /**
     * Deletes a stock from the favorites of a specific investment account.
     *
     * @param request The [ServerRequest] containing the query parameters `investmentAccountId` and `stockSymbol`.
     * @return A [Mono] emitting the server response indicating success or error.
     * @throws IllegalArgumentException if `investmentAccountId` or `stockSymbol` is missing in the request.
     */
    fun deleteFavorites(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }
        val stockSymbol = request.queryParam("stockSymbol")
            .orElseThrow { IllegalArgumentException("stockSymbol is required") }

        logger.info("Deleting favorite stock from investment account with id $investmentAccountId and stock symbol $stockSymbol")

        return favoriteService.removeFavoriteFromInvestmentAccount(investmentAccountId.toLong(), stockSymbol)
            .then(ServerResponse.ok().build())
            .onErrorResume { error ->
                when (error) {
                    is NoSuchElementException -> {
                        logger.warn("Stock symbol $stockSymbol not found for account $investmentAccountId")
                        ServerResponse.notFound().build()
                    }
                    else -> {
                        logger.error("Error deleting favorite stock for account $investmentAccountId", error)
                        ServerResponse.badRequest().bodyValue(error.message ?: "Unknown error")
                    }
                }
            }
    }
}
