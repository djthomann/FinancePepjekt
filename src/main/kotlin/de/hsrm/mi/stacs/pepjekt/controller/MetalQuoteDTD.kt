package de.hsrm.mi.stacs.pepjekt.controller

import java.math.BigDecimal

data class MetalQuoteDTD(
    val metal: String,
    val symbol: String,
    val price: BigDecimal,
)