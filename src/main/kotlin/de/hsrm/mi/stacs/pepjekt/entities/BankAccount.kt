package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("bankaccount")
data class BankAccount(
    @Id
    var id: Long? = null,
    var currency: Currency,
    var balance: BigDecimal = BigDecimal.ZERO
)