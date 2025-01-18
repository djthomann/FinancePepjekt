package de.hsrm.mi.stacs.pepjekt.entities.dtos

import java.math.BigDecimal

class CryptoDTO (
    val symbol: String,
    val name: String,
    var currentPrice: BigDecimal,
)