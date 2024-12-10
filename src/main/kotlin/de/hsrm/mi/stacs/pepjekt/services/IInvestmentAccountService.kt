package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Stock
import java.math.BigDecimal

interface IInvestmentAccountService {

    fun buyStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Void

    fun sellStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Void

    fun getPortfolio(userId: Long): Map<Stock, Float>
}