package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("stock_quote")
data class StockQuote(
    @Id
    var id: Long? = null,
    var currentPrice: BigDecimal,
    var change: Float,
    var percentChange: Float,
    var highPriceOfTheDay: BigDecimal,
    var lowPriceOfTheDay: BigDecimal,
    var openPriceOfTheDay: BigDecimal,
    var previousClosePrice: BigDecimal,
    var timeStamp: LocalDateTime,
    var stockSymbol: String
)
