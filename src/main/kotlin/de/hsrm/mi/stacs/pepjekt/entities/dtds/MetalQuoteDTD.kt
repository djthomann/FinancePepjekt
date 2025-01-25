package de.hsrm.mi.stacs.pepjekt.entities.dtds

import java.math.BigDecimal

data class MetalQuoteDTD(
    val metal: String,
    val symbol: String,
    val price: BigDecimal,
)