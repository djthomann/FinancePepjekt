package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("portfolio_entry")
data class PortfolioEntry(
    @Id
    val id: Long? = null,
    val investmentAccountId: Long,
    val stockSymbol: String,
    val quantity: Double,
    val totalInvestAmount: BigDecimal
)
