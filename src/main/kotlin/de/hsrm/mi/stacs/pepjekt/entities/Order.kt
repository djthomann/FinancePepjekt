package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Table

@Table("stock_order")
data class Order(
    @Id
    var id: Long? = null,
    val volume: Float,
    val type: OrderType,
    val investmentAccountId: Long,
    val stockSymbol: String
)