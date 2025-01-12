package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface IPortfolioEntryRepository: R2dbcRepository<PortfolioEntry, Long> {

    fun findByInvestmentAccountIdAndStockSymbol(investmentAccountId: Long, stockSymbol: String): Mono<PortfolioEntry>

    @Query("SELECT * FROM portfolio_entry WHERE investment_account_id = :investmentAccountId")
    fun findByInvestmentAccountId(investmentAccountId: Long): Flux<PortfolioEntry>

}