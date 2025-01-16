package de.hsrm.mi.stacs.pepjekt.repositories

import de.hsrm.mi.stacs.pepjekt.entities.Metal
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface IMetalRepository: R2dbcRepository<Metal, String> {
}