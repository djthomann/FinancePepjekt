package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.Order
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface IOrderRepository : R2dbcRepository<Order, Long> {

    @Query("SELECT * FROM stock_order o WHERE o.investment_account_id = :investmentAccountId")
    fun findByInvestmentAccountId(investmentAccountId: Long): Flux<Order>
}