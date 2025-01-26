package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.entities.StockQuoteLatest
import de.hsrm.mi.stacs.pepjekt.entities.dtos.StockDetailsDTO
import de.hsrm.mi.stacs.pepjekt.repositories.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    val stockQuoteRepository: IStockQuoteRepository,
    val stockQuoteLatestRepository: IStockQuoteLatestRepository,
    val investmentAccountRepository: IInvestmentAccountRepository,
    val portfolioEntryRepository: IPortfolioEntryRepository,
) : IStockService {

    val logger: Logger = LoggerFactory.getLogger(StockService::class.java)

    /**
     * Retrieves a stock by its symbol.
     *
     * @param symbol the symbol of the stock to retrieve
     * @return a [Mono] emitting the [Stock] corresponding to the symbol, or an error if not found
     */
    override fun getStockBySymbol(symbol: String): Mono<Stock> {
        logger.info("Retrieving stock with symbol: $symbol")
        return stockRepository.findBySymbol(symbol)
            .doOnError { error -> logger.error("Error retrieving stock with symbol: $symbol", error) }
    }

    /**
     * Retrieves detailed information about a stock, including current quotes and portfolio information
     * for a specific investment account.
     *
     * @param symbol the symbol of the stock
     * @param investmentAccountId the ID of the investment account
     * @return a [Mono] emitting the [StockDetailsDTO] containing stock details, or an error if not found
     */
    override fun getStockDetails(symbol: String, investmentAccountId: Long): Mono<StockDetailsDTO> {
        logger.info("Getting stock details for symbol: $symbol and investment account ID: $investmentAccountId")

        val stockMono = getStockBySymbol(symbol)
        val quoteMono = getLatestQuoteBySymbol(symbol)

        return investmentAccountRepository.findById(investmentAccountId)
            .switchIfEmpty(Mono.error(RuntimeException("InvestmentAccount not found")))
            .flatMap { account ->
                portfolioEntryRepository.findByInvestmentAccountIdAndStockSymbol(account.id!!, symbol)
                    .map { portfolioEntry ->
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
        logger.info("Retrieving stock with description: $description")
        return stockRepository.findByDescription(description)
    }

    /**
     * Retrieves all stocks in the database.
     *
     * @return a [Flux] emitting all [Stock] instances in the database
     */
    override fun getStocks(): Flux<Stock> {
        logger.debug("Retrieving all stocks")
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
        logger.info("Calculating average price for stock symbol: $symbol from $from to $to")

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
            .doOnError { error -> logger.error("Error calculating average price for $symbol", error) }
    }

    /**
     * Calculates the average price of a stock based on its historical quote data.
     *
     * This method retrieves all historical quotes for a given stock symbol and calculates
     * the average price by summing the prices and dividing by the total number of quotes.
     *
     * @param symbol the symbol of the stock
     * @return a [Mono] emitting the average price of the stock as a [BigDecimal],
     *         or [BigDecimal.ZERO] if no quotes are found
     */
    override fun calculateAveragePrice(symbol: String): Mono<BigDecimal> {
        logger.info("Calculating average price for stock symbol: $symbol")

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
        logger.info("Retrieving stock history for symbol: $symbol")
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
        logger.info("Retrieving stock history for symbol: $symbol from $from to $to")
        return stockQuoteRepository.findByStockSymbol(symbol)
            .filter { quote -> quote.timeStamp.isAfter(from) && quote.timeStamp.isBefore(to) }
    }

    /**
     * Retrieves the latest quote for a stock by its symbol.
     *
     * @param symbol the symbol of the stock
     * @return a [Mono] emitting the latest [StockQuote] instance, or an error if not found
     */
    override fun getAllStocks(): Flux<Stock> {
        return stockRepository.findAll()
    }

    /**
     * Saves a stock quote into the repository.
     *
     * @param stockQuote the [StockQuote] to save
     * @return a [Mono] emitting the saved [StockQuote]
     */
    override fun saveStockQuote(stockQuote: StockQuote): Mono<StockQuote> {
        logger.debug("Saving stock quote for stock symbol: ${stockQuote.stockSymbol}")
        return stockQuoteRepository.save(stockQuote)
    }

    /**
     * Saves the latest stock quote in the repository.
     *
     * @param stockQuote the [StockQuote] to save as the latest quote
     * @return a [Mono] emitting the saved [StockQuoteLatest]
     */
    override fun saveLatestQuote(stockQuote: StockQuote): Mono<StockQuoteLatest> {
        logger.debug("Saving latest stock quote for stock symbol: ${stockQuote.stockSymbol}")
        val quote = StockQuoteLatest(stockQuote.stockSymbol, stockQuote.id!!)

        return stockQuoteLatestRepository.findById(stockQuote.stockSymbol)
            .flatMap { existingQuote ->
                existingQuote.quoteId = stockQuote.id!!
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
                stockQuoteRepository.findById(entry.quoteId)
            }
    }

}
