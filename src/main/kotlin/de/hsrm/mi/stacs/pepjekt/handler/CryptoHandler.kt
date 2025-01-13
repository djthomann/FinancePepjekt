package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.services.ICryptoService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CryptoHandler(private val cryptoService: ICryptoService) {

    fun getCryptoBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        return cryptoService.getCryptoBySymbol(symbol)
            .flatMap { crypto ->
                ServerResponse.ok().bodyValue(crypto)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    fun getAllCryptos(request: ServerRequest): Mono<ServerResponse> {

        return ServerResponse.ok().body(cryptoService.getAllCryptos(), Crypto::class.java)
            .switchIfEmpty(ServerResponse.noContent().build())
    }

}