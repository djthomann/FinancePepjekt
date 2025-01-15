package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock

class StockDetailsDTO(
    val stock: StockDTO,
    val portfolioEntry: PortfolioEntryDTO?,
) {
    companion object {
        fun mapToDto(
            stock: Stock,
            quote: Quote,
            portfolioEntryToMap: PortfolioEntry?
        ): StockDetailsDTO {
            return StockDetailsDTO(
                stock = StockDTO.mapToDto(stock, quote),
                portfolioEntry = portfolioEntryToMap?.let {
                    PortfolioEntryDTO.mapToDtoWithoutStock(it, quote)
                }
            )
        }
    }
}