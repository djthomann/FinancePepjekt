package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.User
import de.hsrm.mi.stacs.pepjekt.repositories.IFinanceUserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for retrieving the users.
 *
 * This service provides methods for retrieving user details.
 */
@Service
class FinanceUserService(
    val financeUserRepository: IFinanceUserRepository
) : IFinanceUserService {

    override fun getUserId(id: Long): Mono<User> {
        return financeUserRepository.findById(id);
    }

    override fun getUserByInvestmentAccountId(investmentAccount: Long): Mono<User> {
        TODO("Not yet implemented")
    }

}
