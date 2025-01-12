package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("portfolio_entry")
data class PortfolioEntry(
    val investmentAccountId: Long,
    val stockSymbol: String,
    val quantity: Double
)
