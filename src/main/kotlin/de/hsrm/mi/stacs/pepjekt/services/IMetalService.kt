package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface IMetalService {

    fun getMetalBySymbol(symbol: String): Mono<Metal>

    fun getAllMetals(): Flux<Metal>

    fun saveMetalQuote(metalQuote: MetalQuote): Mono<MetalQuote>

    fun saveLatestQuote(metalQuote: MetalQuote): Mono<MetalQuoteLatest>

    fun getLatestMetalQuote(symbol: String): Mono<MetalQuote>
}