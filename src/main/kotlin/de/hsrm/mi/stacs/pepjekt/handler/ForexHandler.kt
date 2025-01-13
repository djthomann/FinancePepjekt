package de.hsrm.mi.stacs.pepjekt.handler

import com.fasterxml.jackson.databind.JsonNode
import de.hsrm.mi.stacs.pepjekt.controller.CoinQuoteDTD
import de.hsrm.mi.stacs.pepjekt.controller.MetalQuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.Currency
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Component
class ForexHandler(
    webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(ForexHandler::class.java)
    private final val forex_webclient = webClientBuilder.baseUrl("https://forex-data-feed.swissquote.com/public-quotes").build()

    fun fetchMetalPrice(symbol: String): Mono<MetalQuoteDTD> {

        logger.info("Fetching metal: $symbol")

        return forex_webclient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/bboquotes/instrument/${symbol}/USD")
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .map { jsonNode ->
                val usdRate = jsonNode[0]["spreadProfilePrices"][0]["bid"]
                MetalQuoteDTD("metal", symbol, BigDecimal(usdRate.asText()))
            }
    }

}