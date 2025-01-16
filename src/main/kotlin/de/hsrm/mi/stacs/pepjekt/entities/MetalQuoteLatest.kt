package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("latest_metal_quote")
class MetalQuoteLatest(
    @Id
    var metalSymbol: String,
    var quote_id: Long,
)