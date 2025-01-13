package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import org.springframework.data.annotation.Id
import java.math.BigDecimal

class PortfolioEntryDTO(
    @Id
    val id: Long? = null,
    val stockSymbol: String,
    val quantity: Double,
    val stock: StockDTO,
    /*
    val currentValue: BigDecimal = BigDecimal.ZERO,
    val amount: BigDecimal = BigDecimal.ZERO,
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
            )
        }
    }
}
