package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuote
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuoteLatest
import de.hsrm.mi.stacs.pepjekt.handler.ForexHandler
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteLatestRepository
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

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
        return cryptoRepository.findById(symbol)
    }

    /**
     * Retrieve all cryptocurrencies from the Database
     */
    override fun getAllCryptos(): Flux<Crypto> {
        return cryptoRepository.findAll()
    }

    /**
     * Save a new CryptoQuote
     */
    override fun saveCryptoQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuote> {
        return cryptoQuoteRepository.save(cryptoQuote)
    }

    /**
     * Overwrite or store the latest quote for a cryptocurrency
     */
    override fun saveLatestQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuoteLatest> {
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
        return cryptoQuoteLatestRepository.findById(symbol)
            .flatMap { entry ->
                cryptoQuoteRepository.findById(entry.quote_id)
        }
    }
}