package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import reactor.core.publisher.Mono
import java.math.BigDecimal

interface IInvestmentAccountService {

    fun buyStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Mono<InvestmentAccount>

    fun sellStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Mono<InvestmentAccount>

    fun getInvestmentAccountPortfolio(userId: Long): Mono<InvestmentAccount>
}