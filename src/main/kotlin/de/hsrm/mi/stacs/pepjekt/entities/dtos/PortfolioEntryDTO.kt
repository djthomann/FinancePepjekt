package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import org.springframework.data.annotation.Id
import java.math.BigDecimal

class PortfolioEntryDTO(
    val investmentAccountId: Long,
    @Id
    val stockSymbol: String,
    val quantity: Double,
    val stock: StockDTO,
    val currentValue: BigDecimal = BigDecimal.ZERO,
    val amount: BigDecimal = BigDecimal.ZERO,
    val change: BigDecimal = BigDecimal.ZERO,
    val changePercentage: BigDecimal = BigDecimal.ZERO
) {
    companion object {
        fun mapToDto(
            portfolioEntry: PortfolioEntry,
            stock: StockDTO,
            currentValue: BigDecimal,
            amount: BigDecimal,
            change: BigDecimal,
            changePercentage: BigDecimal
        ): PortfolioEntryDTO {
            return PortfolioEntryDTO(
                investmentAccountId = portfolioEntry.investmentAccountId,
                stockSymbol = portfolioEntry.stockSymbol,
                quantity = portfolioEntry.quantity,
                stock = stock,
                currentValue = currentValue,
                amount = amount,
                change = change,
                changePercentage = changePercentage
            )
        }
    }
}
