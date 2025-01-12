package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.Stock
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface IStockRepository : R2dbcRepository<Stock, String> {

    fun findBySymbol(symbol: String): Mono<Stock>

    fun save(stock: Stock): Mono<Stock>
}