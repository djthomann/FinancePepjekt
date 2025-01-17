package de.hsrm.mi.stacs.pepjekt.entities.dtos

import java.math.BigDecimal

class MetalDTO (
    val symbol: String,
    val name: String,
    var currentPrice: BigDecimal,
)