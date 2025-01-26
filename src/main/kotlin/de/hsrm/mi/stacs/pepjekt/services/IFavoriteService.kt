package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Favorite
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface for managing favorite stocks for investment accounts.
 *
 * Provides methods to get, add, remove, and check for favorite stocks associated with investment accounts.
 */
interface IFavoriteService {

    fun getFavoritesOfInvestmentAccount(investmentAccountId: Long): Flux<Favorite>

    fun addFavoriteToInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void>

    fun removeFavoriteFromInvestmentAccount(investmentAccountId: Long, stockSymbol: String): Mono<Void>

    fun isFavorite(investmentAccountId: Long, stockSymbol: String): Mono<Boolean>

}