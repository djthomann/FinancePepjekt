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

/**
 * Handler for interacting with the Coinbase API to fetch cryptocurrency exchange rates.
 *
 * This component is responsible for making HTTP requests to the Coinbase API and mapping the
 * response data into application-specific objects for further processing.
 *
 * @param webClientBuilder A builder for creating a `WebClient` to interact with the Coinbase API.
 */
@Component
class CoinbaseHandler(
    webClientBuilder: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(CoinbaseHandler::class.java)
    private final val coinbase_client = webClientBuilder.baseUrl("https://api.coinbase.com/v2/exchange-rates").build()

    /**
     * Fetches the current exchange rate for a given cryptocurrency symbol.
     *
     * @param symbol The symbol of the cryptocurrency (e.g., "BTC" for Bitcoin, "ETH" for Ethereum).
     * @return A `Mono` emitting a `CryptoQuote` object containing the current exchange rate and timestamp.
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
     * Maps a `CryptoQuoteDTD` object to a `CryptoQuote` object.
     *
     * @param symbol The cryptocurrency symbol associated with the exchange rate.
     * @param cryptoQuoteDTD The data transfer object returned by the Coinbase API.
     * @return A `CryptoQuote` object containing the mapped data.
     */
    fun mapToQuote(symbol: String, cryptoQuoteDTD: CryptoQuoteDTD): CryptoQuote {
        return CryptoQuote(
            currentPrice = cryptoQuoteDTD.rate,
            timeStamp = LocalDateTime.now(),
            cryptoSymbol = symbol
        )
    }

}