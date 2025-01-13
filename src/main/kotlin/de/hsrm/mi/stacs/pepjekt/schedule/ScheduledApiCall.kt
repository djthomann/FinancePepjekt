package de.hsrm.mi.stacs.pepjekt.schedule

import de.hsrm.mi.stacs.pepjekt.controller.CoinQuoteDTD
import de.hsrm.mi.stacs.pepjekt.controller.QuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.handler.CoinbaseHandler
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.services.CryptoService
import de.hsrm.mi.stacs.pepjekt.services.StockService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Duration

@Component
class ScheduledApiCall(
    private val coinbaseHandler: CoinbaseHandler,
    private val finnhubHandler: FinnhubHandler,
    private val handler: FinnhubHandler,
    private val stockService: StockService,
    private val cryptoService: CryptoService
) {

    val logger: Logger = LoggerFactory.getLogger(ScheduledApiCall::class.java)

    @PostConstruct
    fun scheduleApiCall() {
        Flux.interval(Duration.ofSeconds(5))
            .flatMap {
                callFinnhub()
            }
            .subscribe(
                { response ->
                    logger.info(response.toString())

                },
                { error -> logger.info("Error occurred: ${error.message}") }
            )
        Flux.interval(Duration.ofSeconds(1))
            .flatMap {
                callCoinbase()
            }
            .subscribe(
                { response ->
                    logger.info(response.toString())
                },
                { error -> logger.error("Error occurred: ${error.message}", error) }
            )
    }

    fun callFinnhub(): Flux<Stock> {
        return stockService.getStocks()
            .flatMap { stock ->
                finnhubHandler.fetchStockQuote(stock.symbol).flatMap { quote ->
                    stockService.setCurrentPrice(BigDecimal(quote.c.toString()), stock.symbol).doOnNext {
                        logger.info("Stock Price updated for ${stock.name}")
                    }
                }
            }
    }

    fun callCoinbase(): Flux<Crypto> {
        return cryptoService.getAllCryptos()
            .flatMap { crypto ->
                coinbaseHandler.fetchCoinRate(crypto.symbol)
                    .flatMap { quote ->
                        cryptoService.setCurrentPrice(BigDecimal(quote.rate.toString()), crypto.symbol).doOnNext {
                            logger.info("Crypto Rate updated for BTC to ${quote.rate}")
                        }
                    }
            }
    }

}