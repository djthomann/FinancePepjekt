package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuote
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuoteLatest
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteLatestRepository
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service responsible for managing and retrieving cryptocurrency data.
 * It handles fetching and storing crypto quotes, as well as maintaining the latest available quote for each cryptocurrency.
 */
@Service
class CryptoService(
    val cryptoRepository: ICryptoRepository,
    val cryptoQuoteRepository: ICryptoQuoteRepository,
    val cryptoQuoteLatestRepository: ICryptoQuoteLatestRepository
) : ICryptoService {

    private val logger = LoggerFactory.getLogger(CryptoService::class.java)

    /**
     * Retrieve a cryptocurrency via its symbol
     */
    override fun getCryptoBySymbol(symbol: String): Mono<Crypto> {
        logger.debug("Fetching cryptocurrency with symbol: $symbol")

        return cryptoRepository.findById(symbol)
    }

    /**
     * Retrieve all cryptocurrencies from the Database
     */
    override fun getAllCryptos(): Flux<Crypto> {
        logger.debug("Fetching all cryptocurrencies")

        return cryptoRepository.findAll()
    }

    /**
     * Save a new CryptoQuote
     */
    override fun saveCryptoQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuote> {
        logger.debug("Saving new cryptocurrency quote for symbol: ${cryptoQuote.cryptoSymbol}")

        return cryptoQuoteRepository.save(cryptoQuote)
    }

    /**
     * Overwrite or store the latest quote for a cryptocurrency
     */
    override fun saveLatestQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuoteLatest> {
        logger.debug("Saving or updating latest quote for cryptocurrency symbol: ${cryptoQuote.cryptoSymbol}")


        val quote = CryptoQuoteLatest(cryptoQuote.cryptoSymbol, cryptoQuote.id!!)

        return cryptoQuoteLatestRepository.findById(cryptoQuote.cryptoSymbol)
            .flatMap { existingQuote ->
                existingQuote.quote_id = cryptoQuote.id!!
                cryptoQuoteLatestRepository.save(existingQuote)
            }
            .switchIfEmpty(
                Mono.defer {
                    cryptoQuoteLatestRepository.save(quote)
                }
            )
    }

    /**
     * Get the latest CryptoQuote via stored latestQuote id
     */
    override fun getLatestCryptoQuote(symbol: String): Mono<CryptoQuote> {
        logger.debug("Fetching latest cryptocurrency quote for symbol: $symbol")

        return cryptoQuoteLatestRepository.findById(symbol)
            .flatMap { entry ->
                cryptoQuoteRepository.findById(entry.quote_id)
        }
    }
}