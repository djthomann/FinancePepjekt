package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IBankAccountService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Handler for managing bank account-related requests.
 * This component handles requests to interact with bank accounts, such as retrieving the balance.
 *
 * @param bankAccountService The service used to perform operations on bank accounts.
 */
@Component
class BankAccountHandler(private val bankAccountService: IBankAccountService) {

    /**
     * Handles a request to get the balance of a bank account.
     *
     * If the bank account ID is not provided or the balance cannot be found, an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with the balance or a 404 not found if no balance is found.
     * @throws IllegalArgumentException If the bank account ID is missing in the query parameters.
     */
    fun getBalance(request: ServerRequest): Mono<ServerResponse> {
        val bankAccountId = request.queryParam("bankAccountId").orElseThrow { IllegalArgumentException("bankAccountId is required") }.toLong()

        return bankAccountService.getBalance(bankAccountId)
            .flatMap { balance ->
                ServerResponse.ok().bodyValue(balance)
            }
            .switchIfEmpty(ServerResponse.notFound().build())
    }

    /**
     * Handles a request to deposit money into a bank account.
     *
     * If the bank account ID is not provided or the balance cannot be found, an error response will be returned.
     *
     * @param request The incoming server request containing query parameters.
     * @return A Mono containing the server response with a 404 not found if the deposit was unsuccessful.
     */
    fun handleDeposit(request: ServerRequest): Mono<ServerResponse> {
        return Mono.zip(
            Mono.justOrEmpty(request.queryParam("bankAccountId"))
                .switchIfEmpty(Mono.error(IllegalArgumentException("bankAccountId is required")))
                .map(String::toLong),
            Mono.justOrEmpty(request.queryParam("amount"))
                .switchIfEmpty(Mono.error(IllegalArgumentException("amount is required")))
                .map { BigDecimal(it) }
        )
            .flatMap { tuple ->
                val bankAccountId = tuple.t1
                val amount = tuple.t2
                bankAccountService.deposit(bankAccountId, amount)
            }
            .then(ServerResponse.ok().build())
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }

    }

}
