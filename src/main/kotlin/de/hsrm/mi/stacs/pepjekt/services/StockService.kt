package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.repositories.IInvestmentAccountRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class StockService(
    val operator: TransactionalOperator, // injected by spring
    val stockRepository: IStockRepository
) : IStockService {

    override fun getStockBySymbol(symbol: String): Stock {
        TODO("Not yet implemented")
    }

    override fun calculateAveragePrice(symbol: String, from: LocalDateTime, to: LocalDateTime): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun getStockHistory(symbol: String): List<Quote> {
        TODO("Not yet implemented")
    }

    override fun getStockHistory(symbol: String, from: LocalDateTime, to: LocalDateTime): List<Quote> {
        TODO("Not yet implemented")
    }
}