package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("metal")
class Metal(
    @Id
    val symbol: String,
    var name: String,
)