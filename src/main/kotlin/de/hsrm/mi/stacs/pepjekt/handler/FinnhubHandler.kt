package de.hsrm.mi.stacs.pepjekt.handler

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
    private val webClient = webClientBuilder.baseUrl("https://finnhub.io/api/v1").build()

    fun fetchStockQuote(symbol: String, token: String): Mono<String> {
        return webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/quote")
                    .queryParam("symbol", symbol)
                    .queryParam("token", token)
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String::class.java)
    }
}