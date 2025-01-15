package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.controller.MarketStatusDTD
import de.hsrm.mi.stacs.pepjekt.controller.QuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.Quote
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

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
    fun fetchStockQuote(symbol: String): Mono<Quote> {
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
            .map { quoteDTD -> mapToQuote(symbol, quoteDTD) }
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

    fun mapToQuote(symbol: String, quoteDTD: QuoteDTD): Quote {
        return Quote(
            currentPrice = BigDecimal.valueOf(quoteDTD.c.toDouble()),
            change = quoteDTD.d,
            percentChange = quoteDTD.dp,
            highPriceOfTheDay = BigDecimal.valueOf(quoteDTD.h.toDouble()),
            lowPriceOfTheDay = BigDecimal.valueOf(quoteDTD.l.toDouble()),
            openPriceOfTheDay = BigDecimal.valueOf(quoteDTD.o.toDouble()),
            previousClosePrice = BigDecimal.valueOf(quoteDTD.pc.toDouble()),
            timeStamp = LocalDateTime.ofEpochSecond(quoteDTD.t, 0, java.time.ZoneOffset.UTC),
            stockSymbol = symbol
        )
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
