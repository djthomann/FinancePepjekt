package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("stock")
data class Stock(
    @Id
    val symbol: String,
    val description: String,
    val figi: String,
    val currency: Currency
) {
}