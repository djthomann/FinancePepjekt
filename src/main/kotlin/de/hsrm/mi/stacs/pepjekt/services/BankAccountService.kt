package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class BankAccountService(
    val operator: TransactionalOperator, // injected by spring
    val investmentAccountRepository: IInvestmentAccountRepository
) : IBankAccountService {

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
                { /* Erfolgreiche Operation */ },
                { error -> println("Error occurred: ${error.message}") }
            )
    }
}
