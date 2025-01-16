package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Metal
import de.hsrm.mi.stacs.pepjekt.repositories.IMetalRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class MetalService(
    val metalRepository: IMetalRepository
) : IMetalService {

    override fun getMetalBySymbol(symbol: String): Mono<Metal> {
        return metalRepository.findById(symbol)
    }

    override fun getAllMetals(): Flux<Metal> {
        return metalRepository.findAll()
    }

    override fun setCurrentPrice(price: BigDecimal, symbol: String): Mono<Metal> {
        return getMetalBySymbol(symbol)
            .flatMap { metal ->
                metal.cprice = price
                metalRepository.save(metal)
            }
    }
}