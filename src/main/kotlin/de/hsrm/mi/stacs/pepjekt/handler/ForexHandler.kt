package de.hsrm.mi.stacs.pepjekt.handler

import com.fasterxml.jackson.databind.JsonNode
import de.hsrm.mi.stacs.pepjekt.entities.dtds.MetalQuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.MetalQuote
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
class ForexHandler(
    webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(ForexHandler::class.java)
    private final val forex_webclient = webClientBuilder.baseUrl("https://forex-data-feed.swissquote.com/public-quotes").build()

    /**
     * Fetches the current bidding price for a given metal symbol
     */
    fun fetchMetalPrice(symbol: String): Mono<MetalQuote> {

        logger.debug("Fetching Metal: $symbol")

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
            .map{ metalQuoteDTD -> mapToQuote(symbol, metalQuoteDTD) }
    }

    /**
     * Map the API DTD to a MetalQuote Object
     */
    fun mapToQuote(symbol: String, metalQuoteDTD: MetalQuoteDTD): MetalQuote {
        return MetalQuote(
            currentPrice = metalQuoteDTD.price,
            timeStamp = LocalDateTime.now(),
            metalSymbol = symbol
        )
    }

}