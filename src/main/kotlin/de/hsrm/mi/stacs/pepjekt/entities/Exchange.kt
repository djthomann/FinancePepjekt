package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("exchange")
data class Exchange(
    @Id
    var id: Long? = null,
    val stocks: List<Stock>,
    val symbol: String,
    val description: String
) {
}