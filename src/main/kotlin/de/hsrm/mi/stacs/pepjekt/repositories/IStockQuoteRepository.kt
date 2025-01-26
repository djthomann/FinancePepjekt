package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface IStockQuoteRepository : R2dbcRepository<StockQuote, Long> {

    fun findByStockSymbol(stockSymbol: String): Flux<StockQuote>
}