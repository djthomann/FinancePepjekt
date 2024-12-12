package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Order
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

interface IOrderService {

    fun placeBuyOrder(
        investmentAccountId: String, stockSymbol: String, volume: BigDecimal, executionTime:
        LocalDateTime
    ): Mono<Void>

    fun placeSellOrder(
        investmentAccountId: String, stockSymbol: String, volume: BigDecimal, executionTime:
        LocalDateTime
    ): Mono<Void>

    fun getOrdersByInvestmentAccount(investmentAccountId: String): Flux<Order>

}