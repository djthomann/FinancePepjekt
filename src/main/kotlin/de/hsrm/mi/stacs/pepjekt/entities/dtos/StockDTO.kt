package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import de.hsrm.mi.stacs.pepjekt.entities.Stock

class StockDTO(
    val symbol: String,
    val description: String,
    val name: String,
    val figi: String,
    val currency: Currency,
    val latestQuote: QuoteDTO,
    val isFavorite: Boolean? = null,
) {
    companion object {
        fun mapToDto(
            stock: Stock,
            stockQuote: StockQuote,
            isFavorite: Boolean? = null
        ): StockDTO {
            return StockDTO(
                symbol = stock.symbol,
                description = stock.description,
                figi = stock.figi,
                currency = stock.currency,
                name = stock.name,
                latestQuote = QuoteDTO.mapToDto(stockQuote),
                isFavorite = isFavorite
            )
        }
    }
}