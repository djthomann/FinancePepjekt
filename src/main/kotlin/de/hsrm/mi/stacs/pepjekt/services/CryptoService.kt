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

    override fun getCryptoBySymbol(symbol: String): Mono<Crypto> {
        return cryptoRepository.findById(symbol)
    }

    override fun getAllCryptos(): Flux<Crypto> {
        return cryptoRepository.findAll()
    }

    override fun saveCryptoQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuote> {
        return cryptoQuoteRepository.save(cryptoQuote)
    }

    override fun saveLatestQuote(cryptoQuote: CryptoQuote): Mono<CryptoQuoteLatest> {
        val quote = CryptoQuoteLatest(cryptoQuote.cryptoSymbol, cryptoQuote.id!!)

        logger.info("Trying to save latest quote")

        return cryptoQuoteLatestRepository.findById(cryptoQuote.cryptoSymbol)
            .flatMap { existingQuote ->
                logger.info("Updating existing quote for symbol: ${cryptoQuote.cryptoSymbol}")
                existingQuote.quote_id = cryptoQuote.id!!
                cryptoQuoteLatestRepository.save(existingQuote)
            }
            .switchIfEmpty(
                Mono.defer {
                    logger.info("No existing quote found for symbol: ${cryptoQuote.cryptoSymbol}. Creating new one.")
                    cryptoQuoteLatestRepository.save(quote)
                }
            )
    }
}