package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
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
    val operator: TransactionalOperator, // injected by spring
    val investmentAccountRepository: IInvestmentAccountRepository
) : IBankAccountService {

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
        return investmentAccountRepository.findByBankAccountId(bankAccountId)
            .switchIfEmpty(Mono.error(NoSuchElementException("No bank account found for ID $bankAccountId")))
            .flatMap { investmentAccount ->
                when {
                    investmentAccount.bankAccount == null ->
                        Mono.error(IllegalStateException("Bank account is null for investment account with ID $bankAccountId"))

                    investmentAccount.bankAccount.balance == null ->
                        Mono.error(IllegalStateException("Balance is null for bank account with ID $bankAccountId"))

                    else ->
                        Mono.just(investmentAccount.bankAccount.balance!!)
                }
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
    override fun deposit(bankAccountId: Long, amount: BigDecimal) {
        investmentAccountRepository.findByBankAccountId(bankAccountId)
            .switchIfEmpty(Mono.error(NoSuchElementException("No bank account found for ID $bankAccountId")))
            .flatMap { investmentAccount ->
                when {
                    investmentAccount.bankAccount == null ->
                        Mono.error(IllegalStateException("Bank account is null for investment account with ID $bankAccountId"))

                    investmentAccount.bankAccount.balance == null ->
                        Mono.error(IllegalStateException("Balance is null for bank account with ID $bankAccountId"))

                    else -> {
                        investmentAccount.bankAccount.balance = investmentAccount.bankAccount.balance!!.plus(amount)
                        investmentAccountRepository.save(investmentAccount).`as`(operator::transactional)
                    }
                }
            }
            .subscribe(
                { /* Erfolgreiche Operation */ },
                { error -> println("Error occurred: ${error.message}") }
            )
    }

    /**
     * Withdraws an amount from a bank account by its ID.
     *
     * @param bankAccountId the ID of the bank account to withdraw the amount from
     * @param amount the amount to withdraw
     * @throws NoSuchElementException if no bank account exists for the given ID
     * @throws IllegalStateException if the bank account or its balance is null
     */
    override fun withdraw(bankAccountId: Long, amount: BigDecimal) {
        investmentAccountRepository.findByBankAccountId(bankAccountId)
            .switchIfEmpty(Mono.error(NoSuchElementException("No bank account found for ID $bankAccountId")))
            .flatMap { investmentAccount ->
                when {
                    investmentAccount.bankAccount == null ->
                        Mono.error(IllegalStateException("Bank account is null for investment account with ID $bankAccountId"))

                    investmentAccount.bankAccount.balance == null ->
                        Mono.error(IllegalStateException("Balance is null for bank account with ID $bankAccountId"))

                    else -> {
                        investmentAccount.bankAccount.balance = investmentAccount.bankAccount.balance!!.minus(amount)
                        investmentAccountRepository.save(investmentAccount).`as`(operator::transactional)
                    }
                }
            }
            .subscribe(
                { /* Successful Operation */ },
                { error -> println("Error occurred: ${error.message}") }
            )
    }
}
