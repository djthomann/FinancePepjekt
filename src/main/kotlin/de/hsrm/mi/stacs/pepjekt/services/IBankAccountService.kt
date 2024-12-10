package de.hsrm.mi.stacs.pepjekt.services

import java.math.BigDecimal

interface IBankAccountService {

    fun getBalance(bankAccountId: Long): BigDecimal

    fun deposit(bankAccountId: Long, amount: BigDecimal): Void

    fun withdraw(bankAccountId: Long, amount: BigDecimal): Void
}