package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IInvestmentAccountService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Component
class InvestmentAccountHandler(private val investmentAccountService: IInvestmentAccountService) {

    fun getPortfolio(request: ServerRequest): Mono<ServerResponse> {
        val userId = request.queryParam("userId").orElseThrow { IllegalArgumentException("userId is required") }.toLong()

        return investmentAccountService.getInvestmentAccountPortfolio(userId)
            .flatMap { portfolio ->
                ServerResponse.ok().bodyValue(portfolio)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

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
