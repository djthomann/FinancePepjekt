package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.controller.MarketStatusDTD
import de.hsrm.mi.stacs.pepjekt.controller.StockQuoteDTD
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
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

    @Value("\${takeFinnhubDummyClient}")
    private var finnhubDummyIsTaken: Boolean = false

    private final val exchange = "US"

    private final val finnhub_webClient = webClientBuilder.baseUrl("https://finnhub.io/api/v1").build()
    private final val dummy_finnhub_webClient = webClientBuilder.baseUrl("http://localhost:8081/api").build()

    private var isMarketOpen = true;

    private val logger = LoggerFactory.getLogger(FinnhubHandler::class.java)

    /**
     * Fetches the Quote, depending on if the market is open or not. Is the market closed, the dummy finnhub webclient
     * will be triggered. If the market is open then finnhub itself.
     */
    fun fetchStockQuote(symbol: String): Mono<StockQuote> {
        var webClient = if (isMarketOpen) {
            finnhub_webClient
        } else {
            dummy_finnhub_webClient
        }

        logger.info(
            "Fetching Stock: {} from {}",
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
            .bodyToMono(StockQuoteDTD::class.java)
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

    /**
     * Maps the API DTD to a StockQuote Object
     */
    fun mapToQuote(symbol: String, stockQuoteDTD: StockQuoteDTD): StockQuote {
        return StockQuote(
            currentPrice = BigDecimal.valueOf(stockQuoteDTD.c.toDouble()),
            change = stockQuoteDTD.d,
            percentChange = stockQuoteDTD.dp,
            highPriceOfTheDay = BigDecimal.valueOf(stockQuoteDTD.h.toDouble()),
            lowPriceOfTheDay = BigDecimal.valueOf(stockQuoteDTD.l.toDouble()),
            openPriceOfTheDay = BigDecimal.valueOf(stockQuoteDTD.o.toDouble()),
            previousClosePrice = BigDecimal.valueOf(stockQuoteDTD.pc.toDouble()),
            timeStamp = LocalDateTime.ofEpochSecond(stockQuoteDTD.t, 0, java.time.ZoneOffset.UTC),
            stockSymbol = symbol
        )
    }

    /**
     * On Application start there is a marketOpen check.
     * If variable takeFinnhubDummyClient is set to true then it will alwqys take the dummy cient.
     * If not it depends if the market is open or not.
     * */
    @PostConstruct
    fun init() {

        //Check if marketStatus is open and then choose between finnhub or the dummy one
        if (!finnhubDummyIsTaken) {
            fetchMarketStatus(exchange).doOnNext { marketStatus ->
                isMarketOpen = marketStatus.isOpen
            }.subscribe()
        } else {
            isMarketOpen = false;
        }

    }
}
