package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.BankAccount
import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.test.Test

class BankAccountServiceTest {
    private val investmentAccountRepository: IInvestmentAccountRepository =
        mock(IInvestmentAccountRepository::class.java)
    private val operator: TransactionalOperator = mock(TransactionalOperator::class.java)

    private lateinit var bankAccountService: BankAccountService
    private lateinit var bankAccount: BankAccount
    private lateinit var investmentAccount: InvestmentAccount

    @BeforeEach
    fun setUp() {
        bankAccount = BankAccount(
            id = 1L,
            balance = BigDecimal(150),
            currency = Currency.USD
        )
        investmentAccount = InvestmentAccount(bankAccount = bankAccount, id = 2L)

        `when`(investmentAccountRepository.findByBankAccountId(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.save(any())).thenReturn(Mono.just(investmentAccount))

        bankAccountService = BankAccountService(operator, investmentAccountRepository)
    }

    @Test
    fun `test getBalance with Data`() {
        bankAccountService.getBalance(1L)
            .doOnNext { balance -> assertThat(balance).isEqualTo(BigDecimal(150)) }
    }

    @Test
    fun `test deposit method`() {
        bankAccountService.deposit(1L, BigDecimal(50))

        verify(investmentAccountRepository).save(argThat {
            bankAccount.balance == BigDecimal(200)
        })
    }

    @Test
    fun `test withdraw method`() {
        bankAccountService.withdraw(1L, BigDecimal(50))

        verify(investmentAccountRepository).save(argThat {
            bankAccount.balance == BigDecimal(100)
        })
    }

}
