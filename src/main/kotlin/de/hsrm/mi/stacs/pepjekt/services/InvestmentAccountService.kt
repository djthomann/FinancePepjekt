package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.math.BigDecimal

@Service
class InvestmentAccountService(
    val operator: TransactionalOperator, // injected by spring
    val investmentAccountRepository: IInvestmentAccountRepository
) : IInvestmentAccountService {

    override fun buyStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Void {
        TODO("Not yet implemented")
    }

    override fun sellStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Void {
        TODO("Not yet implemented")
    }

    override fun getPortfolio(userId: Long): Map<Stock, Float> {
        TODO("Not yet implemented")
    }
}