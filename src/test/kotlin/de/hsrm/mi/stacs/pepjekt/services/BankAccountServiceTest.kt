package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.BankAccount
import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.repositories.IBankAccountRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.test.Test

/**
 * Unit tests for the [BankAccountService] class.
 *
 * This class verifies the functionality of `BankAccountService` methods using mocked dependencies
 * for the repository and transaction operator.
 */
class BankAccountServiceTest {
    private val investmentAccountRepository: IInvestmentAccountRepository = mock(IInvestmentAccountRepository::class.java)
    private val bankAccountRepository: IBankAccountRepository = mock(IBankAccountRepository::class.java)
    private val operator: TransactionalOperator = mock(TransactionalOperator::class.java)

    private lateinit var bankAccountService: BankAccountService
    private lateinit var bankAccount: BankAccount
    private lateinit var investmentAccount: InvestmentAccount

    /**
     * Initializes the test environment by setting up mock data and dependencies.
     */
    @BeforeEach
    fun setUp() {
        bankAccount = BankAccount(
            id = 1L,
            balance = BigDecimal(150),
            currency = Currency.USD
        )
        investmentAccount = InvestmentAccount(bankAccountId = bankAccount.id, id = 2L, ownerId = 1L)

        `when`(investmentAccountRepository.findByBankAccountId(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.save(any())).thenReturn(Mono.just(investmentAccount))

        bankAccountService = BankAccountService(operator, bankAccountRepository, investmentAccountRepository)
    }

    /**
     * Tests the [BankAccountService.getBalance] method to ensure it returns the correct balance.
     */
    @Test
    fun `test getBalance with Data`() {
        bankAccountService.getBalance(1L)
            .doOnNext { balance -> assertThat(balance).isEqualTo(BigDecimal(150)) }
    }

    /**
     * Tests the [BankAccountService.deposit] method to ensure it correctly updates the balance.
     */
    @Test
    fun `test deposit method`() {
        bankAccountService.deposit(1L, BigDecimal(50))

        verify(investmentAccountRepository).save(argThat {
            bankAccount.balance == BigDecimal(200)
        })
    }

    /**
     * Tests the [BankAccountService.withdraw] method to ensure it correctly updates the balance.
     */
    @Test
    fun `test withdraw method`() {
        bankAccountService.withdraw(1L, BigDecimal(50))

        verify(investmentAccountRepository).save(argThat {
            bankAccount.balance == BigDecimal(100)
        })
    }
}
