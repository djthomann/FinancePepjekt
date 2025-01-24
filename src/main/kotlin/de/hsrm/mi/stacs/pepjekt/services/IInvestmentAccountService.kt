package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.entities.dtos.InvestmentAccountDTO
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Interface for managing investment account operations.
 *
 * Provides methods for buying and selling stocks in an investment account, as well as retrieving
 * the portfolio details of an investment account.
 */
interface IInvestmentAccountService {

    fun buyStock(investmentAccountId: Long, stockSymbol: String, purchaseAmount: BigDecimal): Mono<InvestmentAccount>

    fun sellStock(investmentAccountId: Long, stockSymbol: String, volume: Double): Mono<InvestmentAccount>

    fun getInvestmentAccountPortfolio(investmentAccountId: Long): Mono<InvestmentAccountDTO>

    fun getInvestmentAccount(investmentAccountId: Long): Mono<InvestmentAccount>

}