package de.hsrm.mi.stacs.pepjekt.handler

import com.fasterxml.jackson.databind.JsonNode
import de.hsrm.mi.stacs.pepjekt.entities.dtds.CryptoQuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuote
import de.hsrm.mi.stacs.pepjekt.entities.Currency
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
class CoinbaseHandler(
    webClientBuilder: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(CoinbaseHandler::class.java)
    private final val coinbase_client = webClientBuilder.baseUrl("https://api.coinbase.com/v2/exchange-rates").build()

    /**
     * Fetches the current exchange rate for a given Crypto symbol
     */
    fun fetchCoinRate(symbol: String): Mono<CryptoQuote> {
        logger.debug("Fetching Crypto: $symbol")

        return coinbase_client.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .queryParam("currency", symbol)
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .map { jsonNode ->
                val usdRate = jsonNode["data"]["rates"]["USD"].asText()
                CryptoQuoteDTD(symbol, Currency.USD, BigDecimal(usdRate))
            }
            .map { cryptoQuoteDTD -> mapToQuote(symbol, cryptoQuoteDTD) }

    }

    /**
     * Map the API DTD to a CryptoQuote Object
     */
    fun mapToQuote(symbol: String, cryptoQuoteDTD: CryptoQuoteDTD): CryptoQuote {
        return CryptoQuote(
            currentPrice = cryptoQuoteDTD.rate,
            timeStamp = LocalDateTime.now(),
            cryptoSymbol = symbol
        )
    }

}