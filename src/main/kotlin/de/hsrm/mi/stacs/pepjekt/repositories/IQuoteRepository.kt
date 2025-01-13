package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface IQuoteRepository : R2dbcRepository<Quote, Long> {

    fun findByStockSymbol(stockSymbol: String): Flux<Quote>

    fun findTopByStockSymbolOrderByTimeStampDesc(symbol: String): Mono<Quote>
}