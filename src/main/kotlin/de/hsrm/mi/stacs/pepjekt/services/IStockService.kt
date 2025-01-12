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

    fun calculateAveragePrice(symbol: String, from: LocalDateTime, to: LocalDateTime): Mono<BigDecimal>

    fun getStockHistory(symbol: String): Flux<Quote>

    fun getStockHistory(symbol: String, from: LocalDateTime, to: LocalDateTime): Flux<Quote>

    fun getStocks(): Flux<Stock>

}