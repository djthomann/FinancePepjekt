package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.repositories.IQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IStockRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class StockService(
    val stockRepository: IStockRepository,
    val quoteRepository: IQuoteRepository
) : IStockService {

    override fun getStockBySymbol(symbol: String): Mono<Stock> {
        return stockRepository.findBySymbol(symbol)
    }

    override fun calculateAveragePrice(symbol: String, from: LocalDateTime, to: LocalDateTime): Mono<BigDecimal> {
        return stockRepository.findBySymbol(symbol)
            .flatMap { stock ->
                quoteRepository.findByStock(stock)
                    .filter { quote -> quote.timeStamp.isAfter(from) && quote.timeStamp.isBefore(to) }
                    .collectList()
                    .map { quotes ->
                        if (quotes.isEmpty()) {
                            BigDecimal.ZERO
                        } else {
                            val sum = quotes.fold(BigDecimal.ZERO) { acc, quote -> acc.add(quote.value) }
                            sum.divide(BigDecimal(quotes.size), 2, RoundingMode.HALF_UP)
                        }
                    }
            }
    }

    override fun getStockHistory(symbol: String): Flux<Quote> {
        return stockRepository.findBySymbol(symbol)
            .flatMapMany { stock ->
                quoteRepository.findByStock(stock)
            }
    }

    override fun getStockHistory(symbol: String, from: LocalDateTime, to: LocalDateTime): Flux<Quote> {
        return stockRepository.findBySymbol(symbol)
            .flatMapMany { stock ->
                quoteRepository.findByStock(stock)
                    .filter { quote -> quote.timeStamp.isAfter(from) && quote.timeStamp.isBefore(to) }
            }
    }

}