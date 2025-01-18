package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("metal_quote")
class MetalQuote(
    @Id
    var id: Long? = null,
    var currentPrice: BigDecimal,
    var timeStamp: LocalDateTime,
    var metalSymbol: String
)