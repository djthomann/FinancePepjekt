package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Order
import java.math.BigDecimal
import java.time.LocalDateTime

interface IOrderService {

    fun placeBuyOrder(
        investmentAccountId: String, stockSymbol: String, volume: BigDecimal, executionTime:
        LocalDateTime
    ): Void

    fun placeSellOrder(
        investmentAccountId: String, stockSymbol: String, volume: BigDecimal, executionTime:
        LocalDateTime
    ): Void

    fun getOrdersByInvestmentAccount(investmentAccountId: String): List<Order>

}