package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.entities.User
import reactor.core.publisher.Mono

/**
 * Interface for managing the user.
 *
 * Provides methods for retrieving user details.
 */
interface IFinanceUserService {

    fun getUserId(id: Long): Mono<User>

    fun getUserByInvestmentAccountId(investmentAccount: Long): Mono<User>

}