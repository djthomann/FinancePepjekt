package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuote
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuoteLatest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface for managing cryptocurrency operations.
 *
 * Provides methods for retrieving cryptocurrency information, saving quotes, and
 * managing the latest cryptocurrency quotes in a reactive programming paradigm.
 */
interface ICryptoService {

    fun getCryptoBySymbol(symbol: String): Mono<Crypto>

    fun getAllCryptos(): Flux<Crypto>

    fun saveCryptoQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuote>

    fun saveLatestQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuoteLatest>

    fun getLatestCryptoQuote(symbol: String): Mono<CryptoQuote>

}