package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.dtds.MarketStatusDTD
import de.hsrm.mi.stacs.pepjekt.entities.dtds.StockQuoteDTD
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
 * Component responsible for handling communication with the Finnhub API or a dummy service
 * to retrieve stock market data such as stock quotes and market status.
 * The actual API to be used depends on whether the market is open or not.
 * A Finnhub token is needed, when fetching data from Finnhub.
 *
 * @param webClientBuilder The builder to create WebClient instances for making HTTP requests.
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
    private var isMarketOpen = true
    private val logger = LoggerFactory.getLogger(FinnhubHandler::class.java)

    /**
     * Fetches the stock quote for a given symbol. If the market is open, the actual Finnhub API is used.
     * If the market is closed, a dummy web client will be used to simulate the response.
     *
     * @param symbol The stock symbol for which the quote is to be fetched.
     * @return A Mono emitting the fetched StockQuote object.
     */
    fun fetchStockQuote(symbol: String): Mono<StockQuote> {
        var webClient = if (isMarketOpen) {
            finnhub_webClient
        } else {
            dummy_finnhub_webClient
        }

        logger.debug(
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
     * Fetches the market status for a specific exchange.
     *
     * @param exchange The exchange for which the market status is to be fetched (e.g., "US").
     * @return A Mono emitting the MarketStatusDTD object containing the market's open/closed status.
     */
    fun fetchMarketStatus(exchange: String): Mono<MarketStatusDTD> {
        logger.debug("Fetching market status for exchange: $exchange")

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
     * Maps the data transfer object (DTD) to a StockQuote object.
     *
     * @param symbol The stock symbol.
     * @param stockQuoteDTD The data transfer object containing the stock quote details.
     * @return A StockQuote object representing the stock quote.
     */
    fun mapToQuote(symbol: String, stockQuoteDTD: StockQuoteDTD): StockQuote {
        logger.debug("Mapping StockQuoteDTD to StockQuote for symbol: {}", symbol)

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
     * Initializes the component on application startup. This method checks if the market is open
     * and sets the appropriate flag. If the configuration `takeFinnhubDummyClient` is true, the dummy
     * client is always used regardless of market status.
     */
    @PostConstruct
    fun init() {
        logger.info("Initializing FinnhubHandler...")
        //Check if marketStatus is open and then choose between finnhub or the dummy one
        if (!finnhubDummyIsTaken) {
            fetchMarketStatus(exchange).doOnNext { marketStatus ->
                isMarketOpen = marketStatus.isOpen
                logger.info("Market open status: {}", isMarketOpen)
            }.subscribe()
        } else {
            isMarketOpen = false
            logger.info("Dummy Finnhub client is used, market considered closed.")
        }

    }
}
