package de.hsrm.mi.stacs.pepjekt.services

import reactor.core.publisher.Mono
import java.math.BigDecimal

interface IBankAccountService {

    fun getBalance(bankAccountId: Long): Mono<BigDecimal>

    fun deposit(bankAccountId: Long, amount: BigDecimal)

    fun withdraw(bankAccountId: Long, amount: BigDecimal)
}