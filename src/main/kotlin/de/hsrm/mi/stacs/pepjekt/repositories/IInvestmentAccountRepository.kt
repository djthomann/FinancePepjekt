package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface IInvestmentAccountRepository : R2dbcRepository<InvestmentAccount, Long> {

    fun findByOwnerId(ownerId: Long): Mono<InvestmentAccount>

    fun findByBankAccountId(accountId: Long): Mono<InvestmentAccount>
}