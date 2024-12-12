package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Order
import de.hsrm.mi.stacs.pepjekt.repositories.IOrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
    val operator: TransactionalOperator, // injected by spring
    val orderRepository: IOrderRepository
) : IOrderService {

    override fun placeBuyOrder(
        investmentAccountId: String,
        stockSymbol: String,
        volume: BigDecimal,
        executionTime: LocalDateTime
    ): Void {
        TODO("Not yet implemented")
    }

    override fun placeSellOrder(
        investmentAccountId: String,
        stockSymbol: String,
        volume: BigDecimal,
        executionTime: LocalDateTime
    ): Void {
        TODO("Not yet implemented")
    }

    override fun getOrdersByInvestmentAccount(investmentAccountId: String): List<Order> {
        TODO("Not yet implemented")
    }
}