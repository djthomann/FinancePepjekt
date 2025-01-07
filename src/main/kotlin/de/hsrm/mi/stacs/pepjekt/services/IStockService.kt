package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Interface for managing stock-related operations.
 *
 * Provides methods to retrieve stock information by symbol, calculate the average price of a stock
 * over a specified period, and retrieve historical stock data.
 */
interface IStockService {

    fun getStockBySymbol(symbol: String): Mono<Stock>

    fun getStockByDescription(description: String): Mono<Stock>

    fun calculateAveragePrice(symbol: String, from: LocalDateTime, to: LocalDateTime): Mono<BigDecimal>

    fun getStockHistory(symbol: String): Flux<Quote>

    fun getStockHistory(symbol: String, from: LocalDateTime, to: LocalDateTime): Flux<Quote>

    fun getAllStocks(): Flux<Stock>

    fun getLatestQuoteBySymbol(symbol: String): Mono<Quote>

    fun getDayLow(stockSymbol: String, timeStamp: LocalDateTime): Mono<Quote>

    fun getDayHigh(stockSymbol: String, timeStamp: LocalDateTime): Mono<Quote>
}