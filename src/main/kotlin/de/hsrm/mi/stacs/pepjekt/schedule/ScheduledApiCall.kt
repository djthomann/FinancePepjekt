package de.hsrm.mi.stacs.pepjekt.schedule

import de.hsrm.mi.stacs.pepjekt.controller.QuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
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
    private val finnhubHandler: FinnhubHandler,
    private val handler: FinnhubHandler,
    private val stockService: StockService
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
    }

    fun callFinnhub(): Flux<Stock> {
        return stockService.getStocks()
            .flatMap { stock ->
                finnhubHandler.fetchStockQuote(stock.symbol).flatMap { quote ->
                    stockService.setCurrentPrice(BigDecimal(quote.c.toString()), stock.symbol).doOnNext {
                        logger.info("Price updated for ${stock.name}")
                    }
                }
            }
    }

}