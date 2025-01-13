package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.entities.dtos.InvestmentAccountDTO
import de.hsrm.mi.stacs.pepjekt.entities.dtos.UserDTO
import de.hsrm.mi.stacs.pepjekt.services.IFinanceUserService
import de.hsrm.mi.stacs.pepjekt.services.IInvestmentAccountService
import org.slf4j.LoggerFactory
import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

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
    private val userService: IFinanceUserService
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
     * TODO return InvestmentAccountDTO -> has to be done, already in use
     */
    fun getPortfolio(request: ServerRequest): Mono<ServerResponse> {

        val investmentAccountId = request.queryParam("investmentAccountId").orElseThrow { IllegalArgumentException(" " +
                "investmentaccountId is required") }.toLong()

        logger.info("Fetch InvestmentAccountDTO with investmentAccountId: $investmentAccountId")

        return investmentAccountService.getInvestmentAccountPortfolio(investmentAccountId)
            .flatMap { investmentAccount ->
                val investmentAccountDTO = InvestmentAccountDTO.mapToDto(
                    investmentAccount,
                    bankAccount = TODO(),
                    user = UserDTO.mapToDto(userService.getUserByInvestmentAccountId(investmentAccountId).block()!!),
                    stockService = stockService
                )
                ServerResponse.ok().bodyValue(investmentAccountDTO)
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
                if(stockSymbols.isEmpty()){
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
     * Handles a request to buy stock in an investment account.
     *
     * If any required parameter is missing or invalid, an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the updated investment account or an error response if invalid.
     * @throws IllegalArgumentException If any required parameter (investmentAccountId, stockSymbol, volume) is missing.
     */
    fun buyStock(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .map { it.toLong() }
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        val stockSymbol = request.queryParam("stockSymbol")
            .orElseThrow { IllegalArgumentException("stockSymbol is required") }

        val volume = request.queryParam("volume")
            .map { BigDecimal(it) }
            .orElseThrow { IllegalArgumentException("volume is required") }

        return investmentAccountService.buyStock(investmentAccountId, stockSymbol, volume)
            .flatMap { updatedAccount ->
                ServerResponse.ok().bodyValue(updatedAccount)
            }
    }

    /**
     * Handles a request to sell stock from an investment account.
     *
     * If any required parameter is missing or invalid, an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the updated investment account or an error response if invalid.
     * @throws IllegalArgumentException If any required parameter (investmentAccountId, stockSymbol, volume) is missing.
     */
    fun sellStock(request: ServerRequest): Mono<ServerResponse> {
        val investmentAccountId = request.queryParam("investmentAccountId")
            .map { it.toLong() }
            .orElseThrow { IllegalArgumentException("investmentAccountId is required") }

        val stockSymbol = request.queryParam("stockSymbol")
            .orElseThrow { IllegalArgumentException("stockSymbol is required") }

        val volume = request.queryParam("volume")
            .map { BigDecimal(it) }
            .orElseThrow { IllegalArgumentException("volume is required") }

        return investmentAccountService.sellStock(investmentAccountId, stockSymbol, volume)
            .flatMap { updatedAccount ->
                ServerResponse.ok().bodyValue(updatedAccount)
            }
    }

}
