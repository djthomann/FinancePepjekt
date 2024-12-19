package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.BankAccount
import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.*

class InvestmentAccountServiceTest {
    private val investmentAccountRepository: IInvestmentAccountRepository = mock(IInvestmentAccountRepository::class.java)
    private val stockRepository: IStockRepository = mock(IStockRepository::class.java)
    private val operator: TransactionalOperator = mock(TransactionalOperator::class.java)

    private lateinit var investmentAccountService: InvestmentAccountService
    private lateinit var bankAccount: BankAccount
    private lateinit var investmentAccount: InvestmentAccount
    private lateinit var stock: Stock

    @BeforeEach
    fun setUp() {
        val stockSymbol = "AAPL"
        bankAccount = BankAccount(
            id = 1L,
            balance = BigDecimal(150),
            currency = Currency.USD
        )
        stock = Stock(stockSymbol, "Apple Inc.", "BBG000B9XRY4", Currency.USD)
        investmentAccount = InvestmentAccount(bankAccount = bankAccount, id = 1L)

        `when`(investmentAccountRepository.findByOwnerId(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.save(any())).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findByBankAccountId(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(investmentAccountRepository.findById(1L)).thenReturn(Mono.just(investmentAccount))
        `when`(stockRepository.findBySymbol(stockSymbol)).thenReturn(Mono.just(stock))
        `when`(stockRepository.findById(stockSymbol)).thenReturn(Mono.just(stock))

        investmentAccountService = InvestmentAccountService(operator, investmentAccountRepository, stockRepository)
    }

    @Test
    fun `test sell stock`() {
        investmentAccountService.sellStock(1L, stock.symbol, BigDecimal(10))
            .doOnNext {
                assertThat(it).isNotNull
                it.portfolio[stock]?.let { it1 -> assertThat(it1.compareTo(160)) }
                verify(investmentAccountRepository).save(any())
            }
    }

    @Test
    fun `test buy stock`() {
        investmentAccountService.buyStock(1L, stock.symbol, BigDecimal(10))
            .doOnNext {
                assertThat(it).isNotNull
                it.portfolio[stock]?.let { it1 -> assertThat(it1.compareTo(140)) }
                verify(investmentAccountRepository).save(any())
            }
    }

    @Test
    fun `test get investment account`() {
        investmentAccountService.getInvestmentAccountPortfolio(1L)
            .doOnNext { account -> assertThat(investmentAccount == account) }
    }

}