package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDTO
import de.hsrm.mi.stacs.pepjekt.services.IFavoriteService
import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class FavoriteHandler(
    private val favoriteService: IFavoriteService,
    private val stockService: IStockService
) {

    fun getFavorites(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        return favoriteService.getFavoritesOfInvestmentAccount(investmentAccountId.toLong())
            .flatMap { favorite -> stockService.getStockBySymbol(favorite.stockSymbol) }
            .collectList()
            .flatMap { stocks ->
                if (stocks.isEmpty()) {
                    ServerResponse.notFound().build()
                } else {
                    Flux.fromIterable(stocks)
                        .flatMap { stock ->
                            stockService.getLatestQuoteBySymbol(stock.symbol)
                                .map { quote -> StockDTO.mapToDto(stock, quote, true) }
                        }
                        .collectList()
                        .flatMap { stockDtos ->
                            if (stockDtos.isNotEmpty()) {
                                ServerResponse.ok().bodyValue(stockDtos)
                            } else {
                                ServerResponse.notFound().build()
                            }
                        }
                }
            }
    }

    fun addFavorites(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }
        val stockSymbol = request.queryParam("stockSymbol")
            .orElseThrow { IllegalArgumentException("stockSymbol is required") }

        return favoriteService.addFavoriteToInvestmentAccount(investmentAccountId.toLong(), stockSymbol)
            .then(ServerResponse.ok().build())
            .onErrorResume { error ->
                when (error) {
                    is NoSuchElementException -> ServerResponse.notFound().build()
                    else -> ServerResponse.badRequest().bodyValue(error.message ?: "Unknown error")
                }
            }
    }

    fun deleteFavorites(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }
        val stockSymbol = request.queryParam("stockSymbol")
            .orElseThrow { IllegalArgumentException("stockSymbol is required") }

        return favoriteService.removeFavoriteFromInvestmentAccount(investmentAccountId.toLong(), stockSymbol)
            .then(ServerResponse.ok().build())
            .onErrorResume { error ->
                when (error) {
                    is NoSuchElementException -> ServerResponse.notFound().build()
                    else -> ServerResponse.badRequest().bodyValue(error.message ?: "Unknown error")
                }
            }
    }

}