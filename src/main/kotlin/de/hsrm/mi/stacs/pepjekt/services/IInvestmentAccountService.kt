package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Interface for managing investment account operations.
 *
 * Provides methods for buying and selling stocks in an investment account, as well as retrieving
 * the portfolio details of an investment account.
 */
interface IInvestmentAccountService {

    fun buyStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Mono<InvestmentAccount>

    fun sellStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Mono<InvestmentAccount>

    fun getInvestmentAccountPortfolio(userId: Long): Flux<PortfolioEntry>

    fun getInvestmentAccountOwner(userId: Long): Mono<User>

}