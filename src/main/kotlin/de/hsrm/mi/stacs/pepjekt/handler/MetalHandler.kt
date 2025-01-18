package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.Metal
import de.hsrm.mi.stacs.pepjekt.entities.dtos.MetalDTO
import de.hsrm.mi.stacs.pepjekt.services.IMetalService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class MetalHandler(val metalService: IMetalService) {

    /**
     * Handles a request to retrieve a metal by its key (symbol)
     *
     * If no metal is found, a 404 Not Found response will be returned.
     *
     * @param request The incoming server request containing the investment account ID.
     * @return A Mono containing the server response with metal or a 404 if no orders are found.
     * @throws IllegalArgumentException If the investmentAccountId is missing.
     */
    fun getMetalBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        return metalService.getMetalBySymbol(symbol)
            .flatMap { metal ->
                metalService.getLatestMetalQuote(metal.symbol)
                    .map { quote ->
                        MetalDTO(metal.symbol, metal.name, quote.currentPrice)
                    }
                    .flatMap {
                        ServerResponse.ok().bodyValue(it)
                    }
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    /**
     * Handles a request to retrieve all metals
     *
     * If no metals are found, a 404 Not Found response will be returned.
     *
     * @param request The incoming server request containing the investment account ID.
     * @return A Mono containing the server response with all metals or a 404 if no orders are found.
     * @throws IllegalArgumentException If the investmentAccountId is missing.
     */
    fun getMetals(request: ServerRequest): Mono<ServerResponse> {

        return ServerResponse.ok().body(metalService.getAllMetals(), Metal::class.java)
            .switchIfEmpty(ServerResponse.noContent().build())
    }
}