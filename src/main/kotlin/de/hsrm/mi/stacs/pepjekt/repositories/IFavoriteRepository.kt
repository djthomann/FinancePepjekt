package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.Favorite
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface IFavoriteRepository : R2dbcRepository<Favorite, Long> {

    fun findAllByInvestmentAccountId(investmentAccountId: Long): Flux<Favorite>

    fun removeByInvestmentAccountIdAndStockSymbol(investmentAccountId: Long, stockSymbol: String): Mono<Boolean>

    fun findByInvestmentAccountIdAndStockSymbol(investmentAccountId: Long, stockSymbol: String): Mono<Boolean>

}