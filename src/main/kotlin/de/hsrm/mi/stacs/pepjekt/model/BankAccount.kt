package de.hsrm.mi.stacs.pepjekt.model

import java.math.BigDecimal

class BankAccount(var currency: Currency) {
    var balance: BigDecimal? = BigDecimal.ZERO
}