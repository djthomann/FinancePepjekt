package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface IQuoteRepository : R2dbcRepository<Quote, Long> {

    fun findByStockSymbol(stockSymbol: String): Flux<Quote>
}