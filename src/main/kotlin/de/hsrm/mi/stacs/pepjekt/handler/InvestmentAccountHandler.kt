package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IInvestmentAccountService
import org.slf4j.LoggerFactory
import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Handler for managing investment account-related requests.
 * This component handles requests related to investment accounts, such as retrieving the portfolio,
 * buying stocks, and selling stocks.
 *
 * @param investmentAccountService The service used to perform operations on investment accounts.
 */
@Component
class InvestmentAccountHandler(
    private val investmentAccountService: IInvestmentAccountService,
    private val stockService: IStockService,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Handles a request to get the portfolio of an investment account.
     *
     * If the user ID is not provided or the portfolio cannot be found, an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the portfolio or a 404 not found if no portfolio is found.
     * @throws IllegalArgumentException If the user ID is missing in the query parameters.
     *
     */
    fun getPortfolio(request: ServerRequest): Mono<ServerResponse> {

        val investmentAccountId = request.queryParam("investmentAccountId").orElseThrow { IllegalArgumentException(" " +
                "investmentaccountId is required") }.toLong()

        logger.info("Fetch InvestmentAccountDTO with investmentAccountId: $investmentAccountId")

        return investmentAccountService.getInvestmentAccountPortfolio(investmentAccountId)
            .flatMap { portfolio ->
                ServerResponse.ok().bodyValue(portfolio)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    /**
     * Handles a request to get the portfolio of an investment account.
     *
     * If the investmentAccountId is not provided or the portfolio cannot be found, an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the total value of the portfolio or a 404 if the portfolio is empty.
     * @throws IllegalArgumentException If the investmentAccountId is missing in the query parameters.
     *
     * TODO -> has to be done, already in use
     */
    fun getPortfolioTotalValue(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .map { it.toLong() }
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        return investmentAccountService.getInvestmentAccountPortfolio(investmentAccountId)
            .flatMap { portfolio ->
                val stockSymbols = portfolio.portfolio.map { it.stockSymbol }
                if (stockSymbols.isEmpty()) {
                    ServerResponse.notFound().build()
                }

                val highestValuesMonos = stockSymbols.map { symbol ->
                    stockService.getLatestQuoteBySymbol(symbol)
                        .map { quote ->
                            quote.currentPrice
                        }
                }

                Flux.merge(highestValuesMonos)
                    .collectList()
                    .flatMap { highestValues ->
                        val totalValue = highestValues.fold(BigDecimal.ZERO) { acc, price ->
                            acc.add(price)
                        }
                        ServerResponse.ok().bodyValue(totalValue)
                    }
            }
    }

    /**
     * Handles a request to get the bank account linked with an investment account.
     *
     * If the investmentAccountId is not provided an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the id of the bank account or a 404 if the portfolio is empty.
     * @throws IllegalArgumentException If the investmentAccountId is missing in the query parameters.
     */
    fun getBankAccount(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .map { it.toLong() }
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        return investmentAccountService.getBankAccountId(investmentAccountId)
            .flatMap { id ->
                ServerResponse.ok().bodyValue(id)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }
}
