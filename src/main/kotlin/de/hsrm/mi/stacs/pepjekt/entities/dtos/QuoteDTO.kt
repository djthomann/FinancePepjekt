package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

class QuoteDTO(
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
) {
    companion object {
        fun mapToDto(
            quote: Quote,
        ): QuoteDTO {
            return QuoteDTO(
                id = quote.id,
                currentPrice = quote.currentPrice,
                change = quote.change,
                percentChange = quote.percentChange,
                highPriceOfTheDay = quote.highPriceOfTheDay,
                lowPriceOfTheDay = quote.lowPriceOfTheDay,
                openPriceOfTheDay = quote.openPriceOfTheDay,
                previousClosePrice = quote.previousClosePrice,
                timeStamp = quote.timeStamp,
                stockSymbol = quote.stockSymbol
            )
        }
    }
}
