package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import java.math.BigDecimal

class BankAccountDTO(
    var id: Long? = null,
    var currency: Currency,
    var balance: BigDecimal = BigDecimal.ZERO
)