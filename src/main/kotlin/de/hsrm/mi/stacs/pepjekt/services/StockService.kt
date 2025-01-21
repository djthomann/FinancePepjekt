package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuoteLatest
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.entities.dtos.QuoteDTO
import de.hsrm.mi.stacs.pepjekt.entities.StockQuoteLatest
import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDetailsDTO
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
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
    val stockQuoteRepository: IStockQuoteRepository,
    val stockQuoteLatestRepository: IStockQuoteLatestRepository,
    val databaseClient: DatabaseClient,
    val investmentAccountRepository: IInvestmentAccountRepository,
    val portfolioEntryRepository: IPortfolioEntryRepository,
) : IStockService {

    val log: Logger = LoggerFactory.getLogger(StockService::class.java)

    /**
     * Retrieves a stock by its symbol.
     *
     * @param symbol the symbol of the stock to retrieve
     * @return a [Mono] emitting the [Stock] corresponding to the symbol, or an error if not found
     */
    override fun getStockBySymbol(symbol: String): Mono<Stock> {
        return stockRepository.findBySymbol(symbol)
    }


    //TODO Add kdoc
    override fun getStockDetails(symbol: String, investmentAccountId: Long): Mono<StockDetailsDTO> {
        log.info("Getting stockdetails by symbol $symbol and investmentAccountId $investmentAccountId")

        val stockMono = getStockBySymbol(symbol)
        val quoteMono = getLatestQuoteBySymbol(symbol)

        return investmentAccountRepository.findById(investmentAccountId)
            .switchIfEmpty(Mono.error(RuntimeException("InvestmentAccount not found")))
            .flatMap { account ->
                portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(account.id!!, symbol)
                    .map { portfolioEntry -> // Wenn ein Eintrag gefunden wurde
                        Mono.zip(stockMono, quoteMono).map { tuple ->
                            StockDetailsDTO.mapToDto(tuple.t1, tuple.t2, portfolioEntry)
                        }
                    }
                    .defaultIfEmpty(Mono.zip(stockMono, quoteMono).map { tuple ->
                        StockDetailsDTO.mapToDto(tuple.t1, tuple.t2, null)
                    })
                    .flatMap { it }
            }
    }


    /**
     * Retrieves a stock by its description.
     *
     * @param description the description of the stock to retrieve
     * @return a [Mono] emitting the [Stock] corresponding to the description, or an error if not found
     */
    override fun getStockByDescription(description: String): Mono<Stock> {
        return stockRepository.findByDescription(description)
    }

    override fun getStocks(): Flux<Stock> {
        return stockRepository.findAll()
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
        return getStockHistoryBySymbol(symbol, from, to)
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

    override fun calculateAveragePrice(symbol: String): Mono<BigDecimal> {
        return getStockHistoryBySymbol(symbol)
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
     * @return a [Flux] emitting the [StockQuote] instances associated with the stock
     */
    override fun getStockHistoryBySymbol(symbol: String): Flux<StockQuote> {
        return stockRepository.findBySymbol(symbol)
            .flatMapMany {
                stockQuoteRepository.findByStockSymbol(symbol)
            }
    }

    /**
     * Retrieves the historical quote data for a stock within a given time range.
     *
     * @param symbol the symbol of the stock
     * @param from the start of the time range
     * @param to the end of the time range
     * @return a [Flux] emitting the [StockQuote] instances within the specified time range
     */
    override fun getStockHistoryBySymbol(symbol: String, from: LocalDateTime, to: LocalDateTime): Flux<StockQuote> {
        return stockQuoteRepository.findByStockSymbol(symbol)
            .filter { quote -> quote.timeStamp.isAfter(to) && quote.timeStamp.isBefore(from) }

    }

    /**
     * @return all stocks in the database
     */
    override fun getAllStocks(): Flux<Stock> {
        return stockRepository.findAll()
    }

    override fun saveStockQuote(stockQuote: StockQuote): Mono<StockQuote> {
        return stockQuoteRepository.save(stockQuote)
    }

    override fun saveLatestQuote(stockQuote: StockQuote): Mono<StockQuoteLatest> {
        val quote = StockQuoteLatest(stockQuote.stockSymbol, stockQuote.id!!)

        return stockQuoteLatestRepository.findById(stockQuote.stockSymbol)
            .flatMap { existingQuote ->
                existingQuote.quote_id = stockQuote.id!!
                stockQuoteLatestRepository.save(existingQuote)
            }
            .switchIfEmpty(
                Mono.defer {
                    stockQuoteLatestRepository.save(quote)
                }
            )
    }

    /**
     * @param symbol the symbol of the stock
     * @return a [Mono] emitting the [StockQuote] latest instance
     */
    override fun getLatestQuoteBySymbol(symbol: String): Mono<StockQuote> {
        return stockQuoteLatestRepository.findById(symbol)
            .flatMap { entry ->
                stockQuoteRepository.findById(entry.quote_id)
            }
    }

    override fun getDayLow(stockSymbol: String, timeStamp: LocalDateTime): Mono<StockQuote> {
        return databaseClient.sql(
            """
        SELECT * FROM quote o
        WHERE o.stock_symbol = :stockSymbol
        AND DATE(o.time_stamp) = DATE(:timeStamp)
        AND o.low_price_of_the_day = (
            SELECT MIN(low_price_of_the_day) 
            FROM quote 
            WHERE stock_symbol = :stockSymbol 
            AND DATE(time_stamp) = DATE(:timeStamp)
        )
        LIMIT 1
    """
        )
            .bind("stockSymbol", stockSymbol)
            .bind("timeStamp", timeStamp)
            .map { row, metadata ->
                StockQuote(
                    id = row.get("id", Long::class.java) ?: 0L,
                    stockSymbol = row.get("stock_symbol", String::class.java) ?: "",
                    timeStamp = row.get("time_stamp", LocalDateTime::class.java) ?: LocalDateTime.now(),
                    highPriceOfTheDay = row.get("high_price_of_the_day", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    currentPrice = row.get("current_price", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    change = row.get("change", Float::class.java) ?: 0.0f,
                    percentChange = row.get("percent_change", Float::class.java) ?: 0.0f,
                    lowPriceOfTheDay = row.get("low_price_of_the_day", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    openPriceOfTheDay = row.get("open_price_of_the_day", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    previousClosePrice = row.get("previous_close_price", BigDecimal::class.java) ?: BigDecimal.ZERO,
                )
            }
            .one()
    }

    override fun getDayHigh(stockSymbol: String, timeStamp: LocalDateTime): Mono<StockQuote> {
        return databaseClient.sql(
            """
        SELECT * FROM quote o
        WHERE o.stock_symbol = :stockSymbol
        AND DATE(o.time_stamp) = DATE(:timeStamp)
        AND o.high_price_of_the_day = (
            SELECT MAX(high_price_of_the_day) 
            FROM quote 
            WHERE stock_symbol = :stockSymbol 
            AND DATE(time_stamp) = DATE(:timeStamp)
        )
        LIMIT 1
    """
        )
            .bind("stockSymbol", stockSymbol)
            .bind("timeStamp", timeStamp)
            .map { row, metadata ->
                StockQuote(
                    id = row.get("id", Long::class.java) ?: 0L,
                    stockSymbol = row.get("stock_symbol", String::class.java) ?: "",
                    timeStamp = row.get("time_stamp", LocalDateTime::class.java) ?: LocalDateTime.now(),
                    highPriceOfTheDay = row.get("high_price_of_the_day", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    currentPrice = row.get("current_price", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    change = row.get("change", Float::class.java) ?: 0.0f,
                    percentChange = row.get("percent_change", Float::class.java) ?: 0.0f,
                    lowPriceOfTheDay = row.get("low_price_of_the_day", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    openPriceOfTheDay = row.get("open_price_of_the_day", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    previousClosePrice = row.get("previous_close_price", BigDecimal::class.java) ?: BigDecimal.ZERO,
                )
            }
            .one()
    }

    override fun getCurrentValueBySymbol(symbol: String): Mono<BigDecimal> {
        TODO("Not yet implemented")
    }

    override fun getChangeBySymbol(symbol: String): Mono<BigDecimal> {
        TODO("Not yet implemented")
    }

    override fun getChangePercentageBySymbol(symbol: String): Mono<BigDecimal> {
        TODO("Not yet implemented")
    }
}