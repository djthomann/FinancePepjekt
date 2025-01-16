package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import java.math.BigDecimal

class PortfolioEntryDTO(
    val id: Long? = null,
    val stockSymbol: String,
    val quantity: Double,
    val stock: StockDTO? = null,
    val totalValue: BigDecimal = BigDecimal.ZERO,
    /*
    val change: BigDecimal = BigDecimal.ZERO,
    val changePercentage: BigDecimal = BigDecimal.ZERO
     */
) {
    companion object {
        fun mapToDto(
            portfolioEntry: PortfolioEntry,
            stock: StockDTO,
        ): PortfolioEntryDTO {
            return PortfolioEntryDTO(
                id = portfolioEntry.id,
                stockSymbol = portfolioEntry.stockSymbol,
                quantity = portfolioEntry.quantity,
                stock = stock,
                totalValue = stock.latestQuote.currentPrice.multiply(portfolioEntry.quantity.toBigDecimal()),
            )
        }

        fun mapToDtoWithoutStock(portfolioEntry: PortfolioEntry, latestStockQuote: StockQuote): PortfolioEntryDTO {
            return PortfolioEntryDTO(
                id = portfolioEntry.id,
                stockSymbol = portfolioEntry.stockSymbol,
                quantity = portfolioEntry.quantity,
                totalValue = latestStockQuote.currentPrice.multiply(portfolioEntry.quantity.toBigDecimal()),
            )

        }
    }


}
