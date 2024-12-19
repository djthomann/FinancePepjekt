package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IBankAccountService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class BankAccountHandler(private val bankAccountService: IBankAccountService) {

    fun getBalance(request: ServerRequest): Mono<ServerResponse> {
        val bankAccountId = request.queryParam("bankAccountId").orElseThrow { IllegalArgumentException("bankAccountId is required") }.toLong()

        return bankAccountService.getBalance(bankAccountId)
            .flatMap { balance ->
                ServerResponse.ok().bodyValue(balance)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

}
