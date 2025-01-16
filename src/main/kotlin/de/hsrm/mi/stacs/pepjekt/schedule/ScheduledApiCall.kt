package de.hsrm.mi.stacs.pepjekt.schedule

import de.hsrm.mi.stacs.pepjekt.entities.Metal
import de.hsrm.mi.stacs.pepjekt.handler.CoinbaseHandler
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import de.hsrm.mi.stacs.pepjekt.repositories.IQuoteRepository
import de.hsrm.mi.stacs.pepjekt.handler.ForexHandler
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalQuoteRepository
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
import java.math.BigDecimal
import java.time.Duration

@Component
class ScheduledApiCall(
    private val coinbaseHandler: CoinbaseHandler,
    private val finnhubHandler: FinnhubHandler,
    private val forexHandler: ForexHandler,
    private val stockService: StockService,
    private val cryptoService: CryptoService,
    private val metalService: MetalService,
    private val quoteRepository: IQuoteRepository,
    private val cryptoQuoteRepository: ICryptoQuoteRepository,
    private val metalQuoteRepository: IMetalQuoteRepository,
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
                    // logger.info(response.toString())
                },
                { error -> logger.info("Error occurred: ${error.message}") }
            )
        Flux.interval(Duration.ofSeconds(1))
            .flatMap {
                callCoinbase()
            }
            .subscribe(
                { response ->
                    // logger.info(response.toString())
                },
                { error -> logger.error("Error occurred: ${error.message}", error) }
            )
        Flux.interval(Duration.ofSeconds(2))
            .flatMap {
                callForex()
            }
            .subscribe(
                { response ->
                    // logger.info(response.toString())
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

    fun callCoinbase(): Mono<Void> {

        return cryptoService.getAllCryptos()
            .flatMap { crypto ->
                coinbaseHandler.fetchCoinRate(crypto.symbol)
                    .flatMap { quote ->
                        cryptoQuoteRepository.save(quote)
                    }
            }
            .`as`(operator::transactional)
            .then()
    }

    fun callForex(): Mono<Void> {

        return metalService.getAllMetals()
            .flatMap { metal ->
                forexHandler.fetchMetalPrice(metal.symbol)
                    .flatMap { quote ->
                        metalQuoteRepository.save(quote)
                    }
            }
            .`as`(operator::transactional)
            .then()
    }

}