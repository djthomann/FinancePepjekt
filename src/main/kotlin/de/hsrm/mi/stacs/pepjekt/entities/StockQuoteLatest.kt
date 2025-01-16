package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("latest_stock_quote")
class StockQuoteLatest (
    @Id
    var stockSymbol: String,
    var quote_id: Long,
)