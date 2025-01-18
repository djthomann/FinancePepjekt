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
) {
    companion object {
        fun mapToDto(
            stock: Stock,
            stockQuote: StockQuote
        ): StockDTO {
            return StockDTO(
                symbol = stock.symbol,
                description = stock.description,
                figi = stock.figi,
                currency = stock.currency,
                name = stock.name,
                latestQuote = QuoteDTO.mapToDto(stockQuote)
            )
        }
    }
}