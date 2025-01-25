package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Metal
import de.hsrm.mi.stacs.pepjekt.entities.MetalQuote
import de.hsrm.mi.stacs.pepjekt.entities.MetalQuoteLatest
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalQuoteLatestRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service to manage Metals and Metal Quotes.
 * Provides methods to retrieve metals, save metal quotes, and fetch the latest quotes.
 */
@Service
class MetalService(
    val metalRepository: IMetalRepository,
    val metalQuoteRepository: IMetalQuoteRepository,
    val metalQuoteLatestRepository: IMetalQuoteLatestRepository,
) : IMetalService {

    private val logger: Logger = LoggerFactory.getLogger(MetalService::class.java)

    /**
     * Retrieve a single Metal from its symbol.
     *
     * @param symbol The symbol of the metal to fetch.
     * @return A Mono containing the Metal if found, or an empty Mono if not found.
     */
    override fun getMetalBySymbol(symbol: String): Mono<Metal> {
        logger.debug("Fetching Metal with symbol: {}", symbol)
        return metalRepository.findById(symbol)
    }

    /**
     * Retrieve all Metals from the database.
     *
     * @return A Flux containing all metals.
     */
    override fun getAllMetals(): Flux<Metal> {
        logger.debug("Fetching all Metals from the database")
        return metalRepository.findAll()
    }

    /**
     * Store a new MetalQuote in the database.
     *
     * @param metalQuote The MetalQuote to store.
     * @return A Mono containing the saved MetalQuote.
     */
    override fun saveMetalQuote(metalQuote: MetalQuote): Mono<MetalQuote> {
        logger.debug("Saving MetalQuote for Metal symbol: {}", metalQuote.metalSymbol)
        return metalQuoteRepository.save(metalQuote)
    }

    /**
     * Store the latest quote for a metal.
     *
     * @param metalQuote The MetalQuote to update and store as the latest.
     * @return A Mono containing the updated or created MetalQuoteLatest.
     */
    override fun saveLatestQuote(metalQuote: MetalQuote): Mono<MetalQuoteLatest> {
        logger.debug("Saving latest quote for Metal symbol: {}", metalQuote.metalSymbol)
        val quote = MetalQuoteLatest(metalQuote.metalSymbol, metalQuote.id!!)

        return metalQuoteLatestRepository.findById(metalQuote.metalSymbol)
            .flatMap { existingQuote ->
                logger.debug("Found existing latest quote for Metal symbol: {}", metalQuote.metalSymbol)
                existingQuote.quote_id = metalQuote.id!!
                metalQuoteLatestRepository.save(existingQuote)
            }
            .switchIfEmpty(
                Mono.defer {
                    logger.debug("No existing quote found, saving new latest quote for Metal symbol: {}", metalQuote.metalSymbol)
                    metalQuoteLatestRepository.save(quote)
                }
            )
    }

    /**
     * Retrieve the latest MetalQuote via metal symbol.
     *
     * @param symbol The symbol of the metal for which to fetch the latest quote.
     * @return A Mono containing the latest MetalQuote if found.
     */
    override fun getLatestMetalQuote(symbol: String): Mono<MetalQuote> {
        logger.debug("Fetching latest MetalQuote for Metal symbol: {}", symbol)
        return metalQuoteLatestRepository.findById(symbol)
            .flatMap { entry ->
                metalQuoteRepository.findById(entry.quote_id)
            }
    }
}