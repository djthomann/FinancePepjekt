package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("favorite")
data class Favorite (
    @Id
    val id: Long? = null,
    val investmentAccountId: Long,
    val stockSymbol: String,
)