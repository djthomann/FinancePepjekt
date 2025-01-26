package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.BankAccount
import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.repositories.IBankAccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
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
    private val bankAccountRepository: IBankAccountRepository = mock(IBankAccountRepository::class.java)

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

        bankAccountService = BankAccountService(bankAccountRepository)

        `when`(bankAccountRepository.findById(bankAccount.id!!)).thenReturn(Mono.just(bankAccount))
        `when`(bankAccountRepository.save(any())).thenAnswer { invocation ->
            val argument = invocation.arguments[0] as BankAccount
            Mono.just(argument)
        }

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
        bankAccountService.deposit(1L, BigDecimal(50)).block()

        verify(bankAccountRepository).save(argThat { it.balance == BigDecimal(200) })
    }

    /**
     * Tests the [BankAccountService.withdraw] method to ensure it correctly updates the balance.
     */
    @Test
    fun `test withdraw method`() {
        bankAccountService.withdraw(1L, BigDecimal(50)).block()

        verify(bankAccountRepository).save(argThat { it.balance == BigDecimal(100) })
    }

}
