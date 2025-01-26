package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Metal
import de.hsrm.mi.stacs.pepjekt.entities.MetalQuote
import de.hsrm.mi.stacs.pepjekt.entities.MetalQuoteLatest
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalQuoteLatestRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalRepository
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

/**
 * Unit tests for the [MetalService] class.
 */
class MetalServiceTest {

    private val metalRepository: IMetalRepository = mock(IMetalRepository::class.java)
    private val metalQuoteRepository: IMetalQuoteRepository = mock(IMetalQuoteRepository::class.java)
    private val metalQuoteLatestRepository: IMetalQuoteLatestRepository = mock(IMetalQuoteLatestRepository::class.java)

    private lateinit var metalService: MetalService
    private lateinit var metal: Metal
    private lateinit var metalQuote: MetalQuote
    private lateinit var metalQuoteLatest: MetalQuoteLatest

    /**
     * Initializes the test environment by setting up mock data and dependencies.
     */
    @BeforeEach
    fun setUp() {
        metal = Metal(symbol = "GOLD", name = "Gold")
        metalQuote = MetalQuote(
            id = 1L,
            currentPrice = BigDecimal(1800),
            timeStamp = LocalDateTime.now(),
            metalSymbol = metal.symbol
        )
        metalQuoteLatest = MetalQuoteLatest(
            metalSymbol = metal.symbol,
            quote_id = metalQuote.id!!
        )

        metalService = MetalService(metalRepository, metalQuoteRepository, metalQuoteLatestRepository)

        `when`(metalRepository.findById(metal.symbol)).thenReturn(Mono.just(metal))
        `when`(metalRepository.findAll()).thenReturn(Flux.just(metal))

        `when`(metalQuoteRepository.save(metalQuote)).thenReturn(Mono.just(metalQuote))
        `when`(metalQuoteRepository.findById(metalQuote.id!!)).thenReturn(Mono.just(metalQuote))

        `when`(metalQuoteLatestRepository.findById(metal.symbol)).thenReturn(Mono.just(metalQuoteLatest))
        `when`(metalQuoteLatestRepository.save(metalQuoteLatest)).thenReturn(Mono.just(metalQuoteLatest))
    }

    /**
     * Tests the [MetalService.getMetalBySymbol] method to verify that the correct metal is retrieved
     * based on the symbol.
     */
    @Test
    fun `test getMetalBySymbol`() {
        val result = metalService.getMetalBySymbol("GOLD").block()

        assertEquals(result?.symbol, metal.symbol)
        assertEquals(result?.name, metal.name)
    }

    /**
     * Tests the [MetalService.getAllMetals] method to ensure that all metals are retrieved correctly.
     */
    @Test
    fun `test getAllMetals`() {
        val result = metalService.getAllMetals().collectList().block()

        assertEquals(result?.size, 1)
        assertEquals(result?.get(0)?.symbol, metal.symbol)
    }

    /**
     * Tests the [MetalService.saveMetalQuote] method to ensure a new MetalQuote is saved correctly.
     */
    @Test
    fun `test saveMetalQuote`() {
        val result = metalService.saveMetalQuote(metalQuote).block()

        assertEquals(result?.id, metalQuote.id)
        assertEquals(result?.metalSymbol, metalQuote.metalSymbol)
        assertEquals(result?.currentPrice, metalQuote.currentPrice)
    }

    /**
     * Tests the [MetalService.saveLatestQuote] method to ensure the latest MetalQuote is stored or updated
     * correctly in the repository.
     */
    @Test
    fun `test saveLatestQuote when quote exists`() {
        val result = metalService.saveLatestQuote(metalQuote).block()

        assertEquals(result?.metalSymbol, metal.symbol)
        assertEquals(result?.quote_id, metalQuote.id)
    }

    /**
     * Tests the [MetalService.saveLatestQuote] method to ensure the latest MetalQuote is stored correctly
     * when no previous latest quote exists.
     */
    @Test
    fun `test saveLatestQuote when quote does not exist`() {
        val result = metalService.saveLatestQuote(metalQuote).block()

        assertEquals(result?.metalSymbol, metal.symbol)
        assertEquals(result?.quote_id, metalQuote.id)
    }

    /**
     * Tests the [MetalService.getLatestMetalQuote] method to ensure the correct latest quote is retrieved
     * based on the symbol.
     */
    @Test
    fun `test getLatestMetalQuote`() {
        val result = metalService.getLatestMetalQuote("GOLD").block()

        assertEquals(result?.metalSymbol, metal.symbol)
        assertEquals(result?.currentPrice, metalQuote.currentPrice)
    }
}