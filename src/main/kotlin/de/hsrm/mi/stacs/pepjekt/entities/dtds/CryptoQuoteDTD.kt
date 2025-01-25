package de.hsrm.mi.stacs.pepjekt.entities.dtds

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import java.math.BigDecimal

data class CryptoQuoteDTD(
    val coin: String,
    val currency: Currency,
    val rate: BigDecimal,
)