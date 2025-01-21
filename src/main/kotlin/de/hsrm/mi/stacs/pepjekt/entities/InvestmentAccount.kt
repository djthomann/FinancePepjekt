package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("investmentaccount")
data class InvestmentAccount (
    @Id
    val id: Long? = null,
    val bankAccountId: Long? = null,
    val ownerId: Long? = null,
    val totalInvestAmount: BigDecimal? = BigDecimal.ZERO,
)