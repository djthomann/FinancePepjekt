package de.hsrm.mi.stacs.pepjekt.handler

import com.fasterxml.jackson.databind.JsonNode
import de.hsrm.mi.stacs.pepjekt.controller.CoinQuoteDTD
import de.hsrm.mi.stacs.pepjekt.controller.QuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.Currency
import org.slf4j.LoggerFactory
import org.springframework.beans.propertyeditors.CurrencyEditor
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Component
class CoinbaseHandler(
    webClientBuilder: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(CoinbaseHandler::class.java)
    private final val coinbase_client = webClientBuilder.baseUrl("https://api.coinbase.com/v2/exchange-rates").build()

    fun fetchCoinRate(coin: String): Mono<CoinQuoteDTD> {

        logger.info("Fetching coin: $coin")

        return coinbase_client.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .queryParam("currency", coin)
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .map { jsonNode ->
                val usdRate = jsonNode["data"]["rates"]["USD"].asText()
                CoinQuoteDTD(coin, Currency.USD, BigDecimal(usdRate))
            }
    }

}