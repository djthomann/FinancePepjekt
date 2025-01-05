package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("investmentaccount")
data class InvestmentAccount (
    @Id
    val id: Long? = null,
    val bankAccountId: Long? = null,
    val portfolio: List<PortfolioEntry> = emptyList(),
    val userId: Long? = null
)