package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import java.math.BigDecimal

class QuoteDTO(
    var currentPrice: BigDecimal,
    var change: Float,
    var percentChange: Float,
    var highPriceOfTheDay: BigDecimal,
    var lowPriceOfTheDay: BigDecimal,
    var openPriceOfTheDay: BigDecimal,
    var previousClosePrice: BigDecimal,
    var timeStamp: String,
    var stockSymbol: String
) {
    companion object {
        fun mapToDto(
            stockQuote: StockQuote,
        ): QuoteDTO {
            return QuoteDTO(
                currentPrice = stockQuote.currentPrice,
                change = stockQuote.change,
                percentChange = stockQuote.percentChange,
                highPriceOfTheDay = stockQuote.highPriceOfTheDay,
                lowPriceOfTheDay = stockQuote.lowPriceOfTheDay,
                openPriceOfTheDay = stockQuote.openPriceOfTheDay,
                previousClosePrice = stockQuote.previousClosePrice,
                timeStamp = stockQuote.timeStamp.toString(),
                stockSymbol = stockQuote.stockSymbol
            )
        }
    }
}
