package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Metal
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

interface IMetalService {

    fun getMetalBySymbol(symbol: String): Mono<Metal>

    fun getAllMetals(): Flux<Metal>

    fun setCurrentPrice(price: BigDecimal, symbol: String): Mono<Metal>
}