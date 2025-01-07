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

/**
 * Service for managing stock-related operations, including retrieving stock information,
 * calculating average prices, and accessing stock history.
 *
 * This service interacts with stock and quote repositories to perform database operations.
 */
@Service
class StockService(
    val stockRepository: IStockRepository,
    val quoteRepository: IQuoteRepository
) : IStockService {


    /**
     * Retrieves a stock by its symbol.
     *
     * @param symbol the symbol of the stock to retrieve
     * @return a [Mono] emitting the [Stock] corresponding to the symbol, or an error if not found
     */
    override fun getStockBySymbol(symbol: String): Mono<Stock> {
        return stockRepository.findBySymbol(symbol)
    }


    /**
     * Calculates the average price of a stock within a given time range.
     *
     * @param symbol the symbol of the stock
     * @param from the start of the time range
     * @param to the end of the time range
     * @return a [Mono] emitting the average price as a [BigDecimal], or zero if no quotes are found
     */
    override fun calculateAveragePrice(symbol: String, from: LocalDateTime, to: LocalDateTime): Mono<BigDecimal> {
        return quoteRepository.findByStockSymbol(symbol)
            .filter { quote -> quote.timeStamp.isAfter(from) && quote.timeStamp.isBefore(to) }
            .collectList()
            .map { quotes ->
                if (quotes.isEmpty()) {
                    BigDecimal.ZERO
                } else {
                    val sum = quotes.fold(BigDecimal.ZERO) { acc, quote -> acc.add(quote.currentPrice) }
                    sum.divide(BigDecimal(quotes.size), 2, RoundingMode.HALF_UP)
                }
            }

    }

    /**
     * Retrieves the full historical quote data for a stock.
     *
     * @param symbol the symbol of the stock
     * @return a [Flux] emitting the [Quote] instances associated with the stock
     */
    override fun getStockHistory(symbol: String): Flux<Quote> {
        return stockRepository.findBySymbol(symbol)
            .flatMapMany {
                quoteRepository.findByStockSymbol(symbol)
            }
    }

    /**
     * Retrieves the historical quote data for a stock within a given time range.
     *
     * @param symbol the symbol of the stock
     * @param from the start of the time range
     * @param to the end of the time range
     * @return a [Flux] emitting the [Quote] instances within the specified time range
     */
    override fun getStockHistory(symbol: String, from: LocalDateTime, to: LocalDateTime): Flux<Quote> {
        return quoteRepository.findByStockSymbol(symbol)
            .filter { quote -> quote.timeStamp.isAfter(from) && quote.timeStamp.isBefore(to) }

    }

    /**
     * @return all stocks in the database
     */
    override fun getAllStocks(): Flux<Stock> {
        return stockRepository.findAll()
    }

    /**
     * @param symbol the symbol of the stock
     * @return a [Mono] emitting the [Quote] latest instance
     */
    override fun getLatestQuoteBySymbol(symbol: String): Mono<Quote> {
        return quoteRepository.findTopByStockSymbolOrderByTimeStampDesc(symbol)
    }
}