package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.CryptoQuoteLatest
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface ICryptoQuoteLatestRepository: R2dbcRepository<CryptoQuoteLatest, String> {
}