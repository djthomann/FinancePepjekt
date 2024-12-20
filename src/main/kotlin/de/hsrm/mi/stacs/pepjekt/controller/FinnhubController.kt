package de.hsrm.mi.stacs.pepjekt.controller

import de.hsrm.mi.stacs.pepjekt.handler.FinnhubHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController("/api")
class FinnhubController(
    private val finnhubHandler: FinnhubHandler
) {
    private val token = "ct2r2bhr01qiurr42bq0ct2r2bhr01qiurr42bqg"

    @GetMapping("/stock")
    fun getStockQuote(@RequestParam symbol: String): Mono<ResponseEntity<QuoteDTD>> {
        val quoteMono: Mono<QuoteDTD> = finnhubHandler.fetchStockQuote(symbol, token)
        return quoteMono.map { response ->
            ResponseEntity.ok(response)
        }
    }

    @GetMapping("/market-status")
    fun geteMarketStatus(@RequestParam exchange: String): Mono<ResponseEntity<MarketStatusDTD>> {
        val marketStatusMono: Mono<MarketStatusDTD> = finnhubHandler.fetchMarketStatus(exchange, token)
        return marketStatusMono.map { response ->
            ResponseEntity.ok(response)
        }
    }
}