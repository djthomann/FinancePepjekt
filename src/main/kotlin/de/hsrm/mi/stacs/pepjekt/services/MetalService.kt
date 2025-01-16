package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuoteLatest
import de.hsrm.mi.stacs.pepjekt.entities.Metal
import de.hsrm.mi.stacs.pepjekt.entities.MetalQuote
import de.hsrm.mi.stacs.pepjekt.entities.MetalQuoteLatest
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalQuoteLatestRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class MetalService(
    val metalRepository: IMetalRepository,
    val metalQuoteRepository: IMetalQuoteRepository,
    val metalQuoteLatestRepository: IMetalQuoteLatestRepository,
) : IMetalService {

    override fun getMetalBySymbol(symbol: String): Mono<Metal> {
        return metalRepository.findById(symbol)
    }

    override fun getAllMetals(): Flux<Metal> {
        return metalRepository.findAll()
    }

    override fun saveMetalQuote(metalQuote: MetalQuote): Mono<MetalQuote> {
        return metalQuoteRepository.save(metalQuote)
    }

    override fun saveLatestQuote(metalQuote: MetalQuote): Mono<MetalQuoteLatest> {
        val quote = MetalQuoteLatest(metalQuote.metalSymbol, metalQuote.id!!)

        return metalQuoteLatestRepository.findById(metalQuote.metalSymbol)
            .flatMap { existingQuote ->
                existingQuote.quote_id = metalQuote.id!!
                metalQuoteLatestRepository.save(existingQuote)
            }
            .switchIfEmpty(
                Mono.defer {
                    metalQuoteLatestRepository.save(quote)
                }
            )
    }

    override fun getLatestMetalQuote(symbol: String): Mono<MetalQuote> {
        return metalQuoteLatestRepository.findById(symbol)
            .flatMap { entry ->
                metalQuoteRepository.findById(entry.quote_id)
            }
    }
}