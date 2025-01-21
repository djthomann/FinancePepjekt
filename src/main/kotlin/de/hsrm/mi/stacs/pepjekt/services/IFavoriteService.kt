package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Favorite
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface IFavoriteService {

    fun getFavoritesOfInvestmentAccount(investmentAccountId: Long): Flux<Favorite>

    fun addFavoriteToInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void>

    fun removeFavoriteFromInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void>

}