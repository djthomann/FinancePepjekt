package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("stock_order")
data class Order(
    @Id
    var id: Long? = null,
    val purchaseAmount: BigDecimal,
    val purchaseVolume: Double,
    val type: OrderType,
    val executionTimestamp: LocalDateTime,
    val investmentAccountId: Long,
    val stockSymbol: String
)