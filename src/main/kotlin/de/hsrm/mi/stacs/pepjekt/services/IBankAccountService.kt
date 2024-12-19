package de.hsrm.mi.stacs.pepjekt.services

import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Interface for managing bank account operations.
 *
 * Provides methods for retrieving balances, depositing funds, and withdrawing funds
 * from bank accounts in a reactive programming paradigm.
 */
interface IBankAccountService {

    fun getBalance(bankAccountId: Long): Mono<BigDecimal>

    fun deposit(bankAccountId: Long, amount: BigDecimal)

    fun withdraw(bankAccountId: Long, amount: BigDecimal)
}