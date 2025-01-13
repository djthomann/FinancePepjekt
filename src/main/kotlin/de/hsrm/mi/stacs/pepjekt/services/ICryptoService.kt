package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

interface ICryptoService {

    fun getCryptoBySymbol(symbol: String): Mono<Crypto>

    fun getAllCryptos(): Flux<Crypto>

    fun setCurrentPrice(price: BigDecimal, symbol: String): Mono<Crypto>

}