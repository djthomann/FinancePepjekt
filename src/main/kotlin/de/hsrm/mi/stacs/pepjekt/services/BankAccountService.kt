package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.repositories.IBankAccountRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Service for managing bank accounts, including balance retrieval, deposits, and withdrawals.
 *
 * This service interacts with an investment account repository and ensures operations
 * are performed within a transactional context.
 */
@Service
class BankAccountService(
    val bankAccountRepository: IBankAccountRepository,
) : IBankAccountService {

    private val logger: Logger = LoggerFactory.getLogger(BankAccountService::class.java)

    /**
     * Retrieves the balance of a bank account by its ID.
     *
     * @param bankAccountId the ID of the bank account to retrieve the balance for
     * @return a [Mono] emitting the balance as a [BigDecimal], or an error if the bank account is not found
     * or has invalid data
     * @throws NoSuchElementException if no bank account exists for the given ID
     * @throws IllegalStateException if the bank account or its balance is null
     */
    override fun getBalance(bankAccountId: Long): Mono<BigDecimal> {
        logger.info("Retrieving balance for bank account ID: $bankAccountId")

        return bankAccountRepository.findById(bankAccountId)
            .switchIfEmpty(Mono.error(NoSuchElementException("No bank account found for ID $bankAccountId")))
            .flatMap { bankAccount ->
                Mono.justOrEmpty(bankAccount?.balance)
            }
    }

    /**
     * Deposits an amount into a bank account by its ID.
     *
     * @param bankAccountId the ID of the bank account to deposit the amount into
     * @param amount the amount to deposit
     * @throws NoSuchElementException if no bank account exists for the given ID
     * @throws IllegalStateException if the bank account or its balance is null
     */
    override fun deposit(bankAccountId: Long, amount: BigDecimal): Mono<Void> {
        logger.info("Depositing amount: $amount to bank account ID: $bankAccountId")

        return bankAccountRepository.findById(bankAccountId)
            .switchIfEmpty(Mono.error(NoSuchElementException("No bank account found for ID $bankAccountId")))
            .flatMap { bankAccount ->
                val updatedBalance = bankAccount.balance.plus(amount)
                val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                bankAccountRepository.save(updatedBankAccount)
            }
            .then()
    }

    /**
     * Withdraws an amount from a bank account by its ID.
     *
     * @param bankAccountId the ID of the bank account to withdraw the amount from
     * @param amount the amount to withdraw
     * @throws NoSuchElementException if no bank account exists for the given ID
     * @throws IllegalStateException if the bank account or its balance is null
     */
    override fun withdraw(bankAccountId: Long, amount: BigDecimal): Mono<Void> {
        logger.info("Withdrawing amount: $amount from bank account ID: $bankAccountId")

        return bankAccountRepository.findById(bankAccountId)
            .switchIfEmpty(Mono.error(NoSuchElementException("No bank account found for ID $bankAccountId")))
            .flatMap { bankAccount ->
                var updatedBalance = bankAccount.balance.minus(amount)
                if (updatedBalance < BigDecimal.ZERO) {
                    updatedBalance = BigDecimal.ZERO
                }

                val updatedBankAccount = bankAccount.copy(balance = updatedBalance)
                bankAccountRepository.save(updatedBankAccount)
            }
            .then()
    }
}
