package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoQuoteRepository
import de.hsrm.mi.stacs.pepjekt.repositories.ICryptoRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class CryptoService(
    val cryptoRepository: ICryptoRepository,
    val cryptoQuoteRepository: ICryptoQuoteRepository
) : ICryptoService {

    override fun getCryptoBySymbol(symbol: String): Mono<Crypto> {
        return cryptoRepository.findById(symbol)
    }

    override fun getAllCryptos(): Flux<Crypto> {
        return cryptoRepository.findAll()
    }

    override fun setCurrentPrice(price: BigDecimal, symbol: String): Mono<Crypto> {
        return getCryptoBySymbol(symbol)
            .flatMap { crypto ->
                crypto.cprice = price
                cryptoRepository.save(crypto)
            }
    }
}