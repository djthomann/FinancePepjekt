package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import de.hsrm.mi.stacs.pepjekt.entities.Stock

class StockDetailsDTO(
    val stock: StockDTO,
    val portfolioEntry: PortfolioEntryDTO?,
) {
    companion object {
        fun mapToDto(
            stock: Stock,
            stockQuote: StockQuote,
            portfolioEntryToMap: PortfolioEntry?,
            isFavorite: Boolean? = null
        ): StockDetailsDTO {
            return StockDetailsDTO(
                stock = StockDTO.mapToDto(stock, stockQuote, isFavorite),
                portfolioEntry = portfolioEntryToMap?.let {
                    PortfolioEntryDTO.mapToDtoWithoutStock(it, stockQuote)
                }
            )
        }
    }
}