package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Order
import de.hsrm.mi.stacs.pepjekt.entities.OrderType
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IOrderRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class OrderService(
    val operator: TransactionalOperator,
    val orderRepository: IOrderRepository,
    val investmentAccountRepository: IInvestmentAccountRepository,
    val stockRepository: IStockRepository
) : IOrderService {

    override fun placeBuyOrder(
        investmentAccountId: String,
        stockSymbol: String,
        volume: BigDecimal,
        executionTime: LocalDateTime
    ): Mono<Void> {
        return operator.transactional(
            Mono.zip(
                investmentAccountRepository.findById(investmentAccountId.toLong()),
                stockRepository.findBySymbol(stockSymbol)
            ).flatMap { tuple ->
                val (account, stock) = tuple
                val order = Order(
                    volume = volume.toFloat(),
                    type = OrderType.BUY,
                    investmentAccount = account,
                    stock = stock
                )
                orderRepository.save(order).`as`(operator::transactional)
            }.then()
        )
    }

    override fun placeSellOrder(
        investmentAccountId: String,
        stockSymbol: String,
        volume: BigDecimal,
        executionTime: LocalDateTime
    ): Mono<Void> {
        return operator.transactional(
            Mono.zip(
                investmentAccountRepository.findById(investmentAccountId.toLong()),
                stockRepository.findBySymbol(stockSymbol)
            ).flatMap { tuple ->
                val (account, stock) = tuple
                val order = Order(
                    volume = volume.toFloat(),
                    type = OrderType.SELL,
                    investmentAccount = account,
                    stock = stock
                )
                orderRepository.save(order).`as`(operator::transactional)
            }.then()
        )
    }

    override fun getOrdersByInvestmentAccount(investmentAccountId: String): Flux<Order> {
        return orderRepository.findByInvestmentAccountId(investmentAccountId.toLong())
    }
}
