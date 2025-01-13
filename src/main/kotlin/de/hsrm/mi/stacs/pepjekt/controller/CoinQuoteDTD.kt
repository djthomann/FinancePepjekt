package de.hsrm.mi.stacs.pepjekt.controller

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import java.math.BigDecimal

data class CoinQuoteDTD(
    val coin: String,
    val currency: Currency,
    val rate: BigDecimal,
)