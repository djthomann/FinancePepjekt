package de.hsrm.mi.stacs.pepjekt.schedule

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.controller.CoinQuoteDTD
import de.hsrm.mi.stacs.pepjekt.controller.QuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.handler.CoinbaseHandler
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.repositories.IQuoteRepository
import de.hsrm.mi.stacs.pepjekt.services.CryptoService
import de.hsrm.mi.stacs.pepjekt.services.StockService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Duration

@Component
class ScheduledApiCall(
    private val coinbaseHandler: CoinbaseHandler,
    private val finnhubHandler: FinnhubHandler,
    private val stockService: StockService,
    private val cryptoService: CryptoService,
    private val quoteRepository: IQuoteRepository,
    private val operator: TransactionalOperator,
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

    fun callFinnhub(): Mono<Void> {
        return stockService.getStocks()
            .flatMap { stock ->
                finnhubHandler.fetchStockQuote(stock.symbol)
                    .flatMap { quote ->
                        quoteRepository.save(quote)
                    }
            }
            .`as`(operator::transactional)
            .then()
    }

    fun callCoinbase(): Flux<Crypto> {
        return cryptoService.getAllCryptos()
            .flatMap { crypto ->
                coinbaseHandler.fetchCoinRate(crypto.symbol)
                    .flatMap { quote ->
                        cryptoService.setCurrentPrice(BigDecimal(quote.rate.toString()), crypto.symbol).doOnNext {
                            logger.info("Crypto Rate updated for ${crypto.symbol} to ${quote.rate}")
                        }
                    }
            }
    }

}