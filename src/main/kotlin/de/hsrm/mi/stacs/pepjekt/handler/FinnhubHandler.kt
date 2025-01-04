package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.controller.MarketStatusDTD
import de.hsrm.mi.stacs.pepjekt.controller.QuoteDTD
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono

/**
 * Component that handles the connection and dataflow between finnhub ot the dummy one. Finnhub token is needed.
 */
@Component
class FinnhubHandler(
    webClientBuilder: WebClient.Builder
) {

    @Value("\${token}")
    private var token: String? = null
    private final val exchange = "US"

    private final val finnhub_webClient = webClientBuilder.baseUrl("https://finnhub.io/api/v1").build()
    private final val dummy_finnhub_webClient = webClientBuilder.baseUrl("http://localhost:8081/api").build()

    private var isMarketOpen = true;

    private val logger = LoggerFactory.getLogger(FinnhubHandler::class.java)

    /**
     * Fetches the Quote, depending on if the market is open or not. Is the market closed, the dummy finnhub webclient
     * will be triggered. If the market is open then finnhub itself.
     */
    fun fetchStockQuote(symbol: String): Mono<QuoteDTD> {
        val webClient = if (isMarketOpen) {
            finnhub_webClient
        } else {
            dummy_finnhub_webClient
        }

        logger.info(
            "Fetching StockQuote of symbol {} from {}",
            symbol,
            if (isMarketOpen) "finnhub_webclient" else "dummy_finnhub_webClient"
        )

        return webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/quote")
                    .queryParam("symbol", symbol)
                    .apply {
                        if (isMarketOpen) {
                            queryParam("token", token)
                        }
                    }
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(QuoteDTD::class.java)
    }

    /**
     * Fetches the market Status
     */
    fun fetchMarketStatus(exchange: String): Mono<MarketStatusDTD> {
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

    /**
     * On Application start there is a marketOpen check
     * */
    @PostConstruct
    fun init() {
        fetchMarketStatus(exchange).doOnNext { marketStatus ->
            isMarketOpen = marketStatus.isOpen
        }.subscribe()
    }
}
