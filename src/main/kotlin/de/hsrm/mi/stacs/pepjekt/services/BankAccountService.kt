package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.math.BigDecimal

@Service
class BankAccountService(
    val operator: TransactionalOperator, // injected by spring
    val bankAccountRepository: IInvestmentAccountRepository
) : IBankAccountService {

    override fun getBalance(bankAccountId: Long): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun deposit(bankAccountId: Long, amount: BigDecimal): Void {
        TODO("Not yet implemented")
    }

    override fun withdraw(bankAccountId: Long, amount: BigDecimal): Void {
        TODO("Not yet implemented")
    }
}