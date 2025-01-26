package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.dtos.CryptoDTO
import de.hsrm.mi.stacs.pepjekt.services.ICryptoService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import org.slf4j.LoggerFactory

/**
 * Handler responsible for managing requests related to cryptocurrency operations.
 *
 * This component provides endpoints to fetch details about cryptocurrencies,
 * such as retrieving a specific cryptocurrency by its symbol or fetching all available cryptocurrencies.
 *
 * @param cryptoService The service used to interact with cryptocurrency-related data.
 */
@Component
class CryptoHandler(private val cryptoService: ICryptoService) {

    private val logger = LoggerFactory.getLogger(CryptoHandler::class.java)

    /**
     * Handles a request to retrieve a crypto by its key (symbol)
     *
     * If no crypto is found, a 404 Not Found response will be returned.
     *
     * @param request The incoming server request containing the investment account ID.
     * @return A Mono containing the server response with crypto or a 404 if no orders are found.
     * @throws IllegalArgumentException If the investmentAccountId is missing.
     */
    fun getCryptoBySymbol(request: ServerRequest): Mono<ServerResponse> {
        val symbol = request.queryParam("symbol").orElseThrow { IllegalArgumentException("symbol is required") }

        logger.info("Received request to fetch crypto by symbol: $symbol")

        return cryptoService.getCryptoBySymbol(symbol)
            .flatMap { crypto ->
                logger.info("Found crypto: ${crypto.symbol} - ${crypto.name}")

                cryptoService.getLatestCryptoQuote(symbol)
                    .map { quote ->
                        CryptoDTO(crypto.symbol, crypto.name, quote.currentPrice)
                    }
                    .flatMap { stockDto ->
                        ServerResponse.ok().bodyValue(stockDto)
                    }
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    /**
     * Handles a request to retrieve all cryptocurrencies
     *
     * If no cryptos are found, a 404 Not Found response will be returned.
     *
     * @param request The incoming server request containing the investment account ID.
     * @return A Mono containing the server response with all cryptos or a 404 if no orders are found.
     * @throws IllegalArgumentException If the investmentAccountId is missing.
     */
    fun getCryptos(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(cryptoService.getAllCryptos(), Crypto::class.java)
            .switchIfEmpty(ServerResponse.noContent().build())
    }

}