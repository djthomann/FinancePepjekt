package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("order")
data class Order(
    @Id
    var id: Long? = null,
    val volume: Float,
    val type: OrderType
) {
}