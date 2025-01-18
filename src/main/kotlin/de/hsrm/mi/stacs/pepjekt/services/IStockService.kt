package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.*
import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDetailsDTO
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

    fun getStockDetails(symbol: String, investmentAccountId: Long): Mono<StockDetailsDTO>

    fun getStockByDescription(description: String): Mono<Stock>

    fun calculateAveragePrice(symbol: String, from: LocalDateTime, to: LocalDateTime): Mono<BigDecimal>

    fun calculateAveragePrice(symbol: String): Mono<BigDecimal>

    fun getStockHistoryBySymbol(symbol: String): Flux<StockQuote>

    fun getStockHistoryBySymbol(symbol: String, from: LocalDateTime, to: LocalDateTime): Flux<StockQuote>

    fun getAllStocks(): Flux<Stock>

    fun saveStockQuote(stockQuote: StockQuote): Mono<StockQuote>

    fun saveLatestQuote(stockQuote: StockQuote): Mono<StockQuoteLatest>

    fun getLatestQuoteBySymbol(symbol: String): Mono<StockQuote>

    fun getDayLow(stockSymbol: String, timeStamp: LocalDateTime): Mono<StockQuote>

    fun getDayHigh(stockSymbol: String, timeStamp: LocalDateTime): Mono<StockQuote>

    fun getStocks(): Flux<Stock>

    fun getCurrentValueBySymbol(symbol: String): Mono<BigDecimal>

    fun getChangeBySymbol(symbol: String): Mono<BigDecimal>

    fun getChangePercentageBySymbol(symbol: String): Mono<BigDecimal>

}