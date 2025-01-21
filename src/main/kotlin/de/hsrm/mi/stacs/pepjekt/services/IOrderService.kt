package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Order
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Interface for managing orders in an investment account.
 *
 * Provides methods to place buy and sell orders for stocks, as well as retrieving orders
 * associated with an investment account.
 */
interface IOrderService {

    fun placeBuyOrder(
        investmentAccountId: Long, stockSymbol: String, purchaseAmount: BigDecimal, executionTime:
        LocalDateTime
    ): Mono<Order>

    fun placeSellOrder(
        investmentAccountId: Long, stockSymbol: String, volume: Double, executionTime:
        LocalDateTime
    ): Mono<Order>

    fun getOrdersByInvestmentAccountId(investmentAccountId: String): Flux<Order>

    fun processOrders(): Mono<Void>
}