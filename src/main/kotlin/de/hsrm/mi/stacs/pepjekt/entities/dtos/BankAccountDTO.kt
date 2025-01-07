package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("bankaccount")
data class BankAccountDTO(
    @Id
    var id: Long? = null,
    var currency: Currency,
    var balance: BigDecimal = BigDecimal.ZERO
)