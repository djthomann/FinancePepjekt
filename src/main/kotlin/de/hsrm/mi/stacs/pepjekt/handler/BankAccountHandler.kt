package de.hsrm.mi.stacs.pepjekt.handler

import de.hsrm.mi.stacs.pepjekt.services.IBankAccountService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Handler for managing bank account-related requests
 *
 * This component processes incoming server requests related to bank account operations,
 * such as retrieving the balance or making a deposit. It uses the `IBankAccountService`
 * to perform the necessary operations and generates appropriate server responses.
 *
 * @property bankAccountService The service used to interact with bank accounts
 */
@Component
class BankAccountHandler(private val bankAccountService: IBankAccountService) {

    private val logger = LoggerFactory.getLogger(CryptoHandler::class.java)

    /**
     * Handles a request to retrieve the balance of a bank account.
     *
     * @param request The incoming `ServerRequest` containing query parameters.
     * @return A `Mono` emitting a `ServerResponse`:
     * - Returns an HTTP 200 status with the balance if successful.
     * - Returns an HTTP 400 status with an error message if the request is invalid.
     * @throws IllegalArgumentException If the `bankAccountId` query parameter is missing.
     */
    fun getBalance(request: ServerRequest): Mono<ServerResponse> {
        val bankAccountId = request.queryParam("bankAccountId").orElseThrow { IllegalArgumentException("bankAccountId is required") }.toLong()

        logger.info("Getting balance for bankaccount with id: {}", bankAccountId)

        return bankAccountService.getBalance(bankAccountId)
            .flatMap { balance ->
                ServerResponse.ok().bodyValue(balance)
            }
            .onErrorResume { e ->
                ServerResponse.badRequest().bodyValue("Error: ${e.message}")
            }
    }

    /**
     * Handles a request to deposit money into a bank account
     *
     * @param request The incoming `ServerRequest` containing query parameters.
     * @return A `Mono` emitting a `ServerResponse`:
     * - Returns an HTTP 200 status if the deposit is successful.
     * - Returns an HTTP 400 status with an error message if the request is invalid or fails.
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
