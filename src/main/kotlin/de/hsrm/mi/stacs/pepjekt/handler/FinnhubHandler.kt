package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.controller.MarketStatusDTD
import de.hsrm.mi.stacs.pepjekt.controller.QuoteDTD
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono

@Component
class FinnhubHandler(
    webClientBuilder: WebClient.Builder
) {
    private val finnhub_webClient = webClientBuilder.baseUrl("https://finnhub.io/api/v1").build()
    private val dummy_finnhub_webClient = webClientBuilder.baseUrl("https://localhost:8081/api").build()

    fun fetchStockQuote(symbol: String, token: String): Mono<QuoteDTD> {
        return finnhub_webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/quote")
                    .queryParam("symbol", symbol)
                    .queryParam("token", token)
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(QuoteDTD::class.java)
    }

    fun fetchMarketStatus(exchange: String, token: String): Mono<MarketStatusDTD> {
        return finnhub_webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("/stock/market-status")
                    .queryParam("exchange", exchange)
                    .queryParam("token", token)
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(MarketStatusDTD::class.java)
    }
}
