package de.hsrm.mi.stacs.pepjekt.controller

import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class FinnhubController(
    private val finnhubHandler: FinnhubHandler
) {

    private val logger = LoggerFactory.getLogger(FinnhubController::class.java)

    @GetMapping("/stock")
    fun getStockQuote(@RequestParam symbol: String): Mono<ResponseEntity<StockQuote>> {
        logger.info("Getting stock quote by symbol: $symbol")

        val stockQuoteMono: Mono<StockQuote> = finnhubHandler.fetchStockQuote(symbol)
        return stockQuoteMono.map { response ->
            ResponseEntity.ok(response)
        }
    }

    @GetMapping("/market-status")
    fun geteMarketStatus(@RequestParam exchange: String): Mono<ResponseEntity<MarketStatusDTD>> {
        logger.info("Getting market-status by exchange: $exchange")

        val marketStatusMono: Mono<MarketStatusDTD> = finnhubHandler.fetchMarketStatus(exchange)
        return marketStatusMono.map { response ->
            ResponseEntity.ok(response)
        }
    }
}