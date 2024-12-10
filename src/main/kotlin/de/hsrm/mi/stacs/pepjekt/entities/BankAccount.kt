package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("bankaccount")
class BankAccount(var currency: Currency) {
    @Id
    var id: Long? = null
    var balance: BigDecimal? = BigDecimal.ZERO
}