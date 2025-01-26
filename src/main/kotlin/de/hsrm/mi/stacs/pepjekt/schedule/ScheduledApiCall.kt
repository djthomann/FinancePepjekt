package de.hsrm.mi.stacs.pepjekt.schedule

import de.hsrm.mi.stacs.pepjekt.handler.CoinbaseHandler
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.handler.ForexHandler
import de.hsrm.mi.stacs.pepjekt.services.CryptoService
import de.hsrm.mi.stacs.pepjekt.services.MetalService
import de.hsrm.mi.stacs.pepjekt.services.StockService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime


/**
 * ScheduledApiCall component responsible for scheduling periodic API calls to fetch stock, crypto, and metal prices.
 * It calls external services (such as Finnhub, Coinbase, and Forex API) to fetch data and save it to the database.
 */
@Component
class ScheduledApiCall(
    private val coinbaseHandler: CoinbaseHandler,
    private val finnhubHandler: FinnhubHandler,
    private val forexHandler: ForexHandler,
    private val stockService: StockService,
    private val cryptoService: CryptoService,
    private val metalService: MetalService,
    private val operator: TransactionalOperator,
) {

    val logger: Logger = LoggerFactory.getLogger(ScheduledApiCall::class.java)

    /**
     * Initializes the periodic API calls to fetch stock, crypto, and metal prices at scheduled intervals.
     */
    @PostConstruct
    fun scheduleApiCall() {
        Flux.interval(Duration.ofSeconds(1))
            .flatMap {
                callFinnhub()
            }
            .subscribe(
                { response ->
                    logger.debug("Finnhub API call successful: {}", response)
                },
                { error -> logger.info("Error occurred: ${error.message}") }
            )
        Flux.interval(Duration.ofSeconds(2))
            .flatMap {
                callCoinbase()
            }
            .subscribe(
                { response ->
                     logger.debug("Coinbase API call successful: {}", response)
                },
                { error -> logger.error("Error occurred: ${error.message}", error) }
            )
        Flux.interval(Duration.ofSeconds(2))
            .flatMap {
                callForex()
            }
            .subscribe(
                { response ->
                     logger.debug("Forex API call successful: {}", response)
                },
                { error -> logger.error("Error occurred: ${error.message}", error) }
            )
    }

    /**
     * Fetches stock quotes from Finnhub API, processes them, and saves the stock quote to the database.
     *
     * @return A Mono signaling the completion of the operation.
     */
    fun callFinnhub(): Mono<Void> {
        logger.debug("Fetching stock quotes from Finnhub.")

        return stockService.getStocks()
            .flatMap { stock ->
                finnhubHandler.fetchStockQuote(stock.symbol)
                    .flatMap { quote ->
                        quote.timeStamp = LocalDateTime.now()
                        stockService.saveStockQuote(quote)
                            .doOnTerminate {
                                // Warten bis garantiert gespeichert ist. WICHTIG
                            }
                            .flatMap { savedQuote ->
                                stockService.saveLatestQuote(savedQuote)
                            }
                    }
            }
            .`as`(operator::transactional)
            .then()
    }

    /**
     * Fetches cryptocurrency quotes from Coinbase API, processes them, and saves the crypto quote to the database.
     *
     * @return A Mono signaling the completion of the operation.
     */
    fun callCoinbase(): Mono<Void> {
        logger.debug("Fetching crypto quotes from Coinbase.")

        return cryptoService.getAllCryptos()
            .flatMap { crypto ->
                coinbaseHandler.fetchCoinRate(crypto.symbol)
                    .flatMap { quote ->
                        cryptoService.saveCryptoQuote(quote)
                            .doOnTerminate {
                                // Warten bis garantiert gespeichert ist. WICHTIG
                            }
                            .flatMap { savedQuote ->
                                cryptoService.saveLatestQuote(savedQuote)
                            }
                    }
            }
            .`as`(operator::transactional)
            .then()
    }

    /**
     * Fetches metal prices from Forex API, processes them, and saves the metal price to the database.
     *
     * @return A Mono signaling the completion of the operation.
     */
    fun callForex(): Mono<Void> {
        logger.debug("Fetching metal prices from Forex.")

        return metalService.getAllMetals()
            .flatMap { metal ->
                forexHandler.fetchMetalPrice(metal.symbol)
                    .flatMap { quote ->
                        metalService.saveMetalQuote(quote)
                            .doOnTerminate {
                                // Warten bis garantiert gespeichert ist. WICHTIG
                            }
                            .flatMap { savedQuote ->
                                metalService.saveLatestQuote(savedQuote)
                            }
                    }
            }
            .`as`(operator::transactional)
            .then()

    }

}