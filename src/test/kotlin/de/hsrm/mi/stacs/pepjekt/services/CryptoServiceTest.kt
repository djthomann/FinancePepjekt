package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuote
import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuoteLatest
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteLatestRepository
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoRepository
import org.junit.jupiter.api.BeforeEach
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import kotlin.test.Test

/**
 * Unit tests for the [CryptoService] class.
 */
class CryptoServiceTest {

    private val cryptoRepository: ICryptoRepository = mock(ICryptoRepository::class.java)
    private val cryptoQuoteRepository: ICryptoQuoteRepository = mock(ICryptoQuoteRepository::class.java)
    private val cryptoQuoteLatestRepository: ICryptoQuoteLatestRepository = mock(ICryptoQuoteLatestRepository::class.java)

    private lateinit var cryptoService: CryptoService
    private lateinit var crypto: Crypto
    private lateinit var cryptoQuote: CryptoQuote
    private lateinit var cryptoQuoteLatest: CryptoQuoteLatest

    /**
     * Initializes the test environment by setting up mock data and dependencies.
     */
    @BeforeEach
    fun setUp() {
        crypto = Crypto(symbol = "BTC", name = "Bitcoin")
        cryptoQuote = CryptoQuote(
            id = 1L,
            currentPrice = BigDecimal(45000),
            timeStamp = LocalDateTime.now(),
            cryptoSymbol = crypto.symbol
        )
        cryptoQuoteLatest = CryptoQuoteLatest(
            cryptoSymbol = crypto.symbol,
            quote_id = cryptoQuote.id!!
        )

        cryptoService = CryptoService(cryptoRepository, cryptoQuoteRepository, cryptoQuoteLatestRepository)

        `when`(cryptoRepository.findById(crypto.symbol)).thenReturn(Mono.just(crypto))
        `when`(cryptoRepository.findAll()).thenReturn(Flux.just(crypto))

        `when`(cryptoQuoteRepository.save(cryptoQuote)).thenReturn(Mono.just(cryptoQuote))
        `when`(cryptoQuoteRepository.findById(cryptoQuote.id!!)).thenReturn(Mono.just(cryptoQuote))

        `when`(cryptoQuoteLatestRepository.findById(crypto.symbol)).thenReturn(Mono.just(cryptoQuoteLatest))
        `when`(cryptoQuoteLatestRepository.save(cryptoQuoteLatest)).thenReturn(Mono.just(cryptoQuoteLatest))
    }

    /**
     * Tests the [CryptoService.getCryptoBySymbol] method to verify that the correct cryptocurrency is retrieved
     * based on the symbol.
     */
    @Test
    fun `test getCryptoBySymbol`() {
        val result = cryptoService.getCryptoBySymbol("BTC").block()

        assertEquals(result?.symbol, crypto.symbol)
        assertEquals(result?.name, crypto.name)
    }

    /**
     * Tests the [CryptoService.getAllCryptos] method to ensure that all cryptocurrencies are retrieved correctly.
     */
    @Test
    fun `test getAllCryptos`() {
        val result = cryptoService.getAllCryptos().collectList().block()

        assertEquals(result?.size, 1)
        assertEquals(result?.get(0)?.symbol, crypto.symbol)
    }

    /**
     * Tests the [CryptoService.saveCryptoQuote] method to ensure a new CryptoQuote is saved correctly.
     */
    @Test
    fun `test saveCryptoQuote`() {
        val result = cryptoService.saveCryptoQuote(cryptoQuote).block()

        assertEquals(result?.id, cryptoQuote.id)
        assertEquals(result?.cryptoSymbol, cryptoQuote.cryptoSymbol)
        assertEquals(result?.currentPrice, cryptoQuote.currentPrice)
    }

    /**
     * Tests the [CryptoService.saveLatestQuote] method to ensure the latest CryptoQuote is stored or updated
     * correctly in the repository.
     */
    @Test
    fun `test saveLatestQuote when quote exists`() {
        val result = cryptoService.saveLatestQuote(cryptoQuote).block()

        assertEquals(result?.cryptoSymbol, crypto.symbol)
        assertEquals(result?.quote_id, cryptoQuote.id)
        verify(cryptoQuoteLatestRepository).save(any())
    }

    /**
     * Tests the [CryptoService.saveLatestQuote] method to ensure the latest CryptoQuote is stored correctly
     * when no previous latest quote exists.
     */
    @Test
    fun `test saveLatestQuote when quote does not exist`() {
        val result = cryptoService.saveLatestQuote(cryptoQuote).block()

        assertEquals(result?.cryptoSymbol, crypto.symbol)
        assertEquals(result?.quote_id, cryptoQuote.id)
    }

    /**
     * Tests the [CryptoService.getLatestCryptoQuote] method to ensure the correct latest quote is retrieved
     * based on the symbol.
     */
    @Test
    fun `test getLatestCryptoQuote`() {
        val result = cryptoService.getLatestCryptoQuote("BTC").block()

        assertEquals(result?.cryptoSymbol, crypto.symbol)
        assertEquals(result?.currentPrice, cryptoQuote.currentPrice)
    }
}