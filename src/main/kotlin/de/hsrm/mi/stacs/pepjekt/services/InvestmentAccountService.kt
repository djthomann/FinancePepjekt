package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.math.BigDecimal

@Service
class InvestmentAccountService(
    val operator: TransactionalOperator, // injected by spring
    val investmentAccountRepository: IInvestmentAccountRepository,
    val stockRepository: IStockRepository
) : IInvestmentAccountService {

    override fun buyStock(investmentAccountId: Long, stockSymbol: String, volume: BigDecimal): Mono<InvestmentAccount> {
        // get investment account, get stock by stockSymbol, buy stock -> less money on bankaccount
        return Mono.zip(
            investmentAccountRepository.findById(investmentAccountId),
            stockRepository.findById(stockSymbol)
        ).flatMap { tuple ->
            val (investmentAccount, stock) = tuple
            if (investmentAccount.portfolio.containsKey(stock)) {
                // add new stock
                investmentAccount.portfolio[stock] = volume.toFloat()
            } else {
                // add value to existing stock
                investmentAccount.portfolio[stock]?.let { currentVolume ->
                    investmentAccount.portfolio[stock] = currentVolume.plus(volume.toFloat())
                }
            }
            investmentAccount.bankAccount?.balance?.minus(volume)
            investmentAccountRepository.save(investmentAccount).`as`(operator::transactional)
        }
    }

    override fun sellStock(
        investmentAccountId: Long,
        stockSymbol: String,
        volume: BigDecimal
    ): Mono<InvestmentAccount> {
        // get investment account, get stock by stockSymbol, sell stock -> more money on bank account, less in investment account stock
        return Mono.zip(
            investmentAccountRepository.findById(investmentAccountId),
            stockRepository.findById(stockSymbol)
        ).flatMap { tuple ->
            val (investmentAccount, stock) = tuple
            investmentAccount.portfolio[stock]?.let { currentVolume ->
                val newVolume = currentVolume.minus(volume.toFloat())
                if (newVolume > 0) {
                    investmentAccount.portfolio[stock] = newVolume
                } else {
                    investmentAccount.portfolio.remove(stock)
                }
                investmentAccount.bankAccount?.balance?.plus(volume)
            }
            investmentAccountRepository.save(investmentAccount).`as`(operator::transactional)
        }
    }

    override fun getInvestmentAccountPortfolio(userId: Long): Mono<InvestmentAccount> {
        return investmentAccountRepository.findByOwnerId(userId)
    }
}