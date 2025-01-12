package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.*

class OrderDTO(
    @Id
    var id: Long? = null,
    val volume: Float,
    val type: OrderType,
    val investmentAccountId: Long,
    val stockSymbol: String,
    val stock: Stock
)