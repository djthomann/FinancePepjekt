package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.Metal
import de.hsrm.mi.stacs.pepjekt.services.IMetalService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class MetalHandler(val metalService: IMetalService) {

    fun getMetalBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        return metalService.getMetalBySymbol(symbol)
            .flatMap {
                ServerResponse.ok().bodyValue(it)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun getMetals(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(metalService.getAllMetals(), Metal::class.java)
            .switchIfEmpty(ServerResponse.noContent().build())
    }
}