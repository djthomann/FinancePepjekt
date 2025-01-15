package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.Crypto
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ICryptoRepository: R2dbcRepository<Crypto, String> {

}