package de.hsrm.mi.stacs.pepjekt.entities

import java.math.BigDecimal

class BankAccount(var currency: Currency) {
    var balance: BigDecimal? = BigDecimal.ZERO
}