package de.hsrm.mi.stacs.pepjekt.controller

import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import de.hsrm.mi.stacs.pepjekt.entities.dtds.MarketStatusDTD
import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Controller for handling API requests related to stock and market information.
 *
 * This controller provides endpoints to retrieve stock quotes and market status
 * information. It utilizes the `FinnhubHandler` to fetch the data from external sources.
 */
@RestController
@RequestMapping("/api")
class FinnhubController(
    private val finnhubHandler: FinnhubHandler
) {

    private val logger = LoggerFactory.getLogger(FinnhubController::class.java)

    /**
     * Retrieves the stock quote for a given stock symbol.
     *
     * @param symbol The symbol of the stock for which the quote is requested.
     * @return A `Mono` that emits a `ResponseEntity` containing the stock quote information.
     */
    @GetMapping("/stock")
    fun getStockQuote(@RequestParam symbol: String): Mono<ResponseEntity<StockQuote>> {
        logger.info("Getting stock quote by symbol: $symbol")

        val stockQuoteMono: Mono<StockQuote> = finnhubHandler.fetchStockQuote(symbol)
        return stockQuoteMono.map { response ->
            ResponseEntity.ok(response)
        }
    }

    /**
     * Retrieves the market status for a specific exchange.
     *
     * @param exchange The name of the exchange for which the market status is requested.
     * @return A `Mono` that emits a `ResponseEntity` containing the market status information.
     */
    @GetMapping("/market-status")
    fun getMarketStatus(@RequestParam exchange: String): Mono<ResponseEntity<MarketStatusDTD>> {
        logger.info("Getting market-status by exchange: $exchange")

        val marketStatusMono: Mono<MarketStatusDTD> = finnhubHandler.fetchMarketStatus(exchange)
        return marketStatusMono.map { response ->
            ResponseEntity.ok(response)
        }
    }
}