package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.PortfolioEntry
import de.hsrm.mi.stacs.pepjekt.entities.StockQuote
import java.math.BigDecimal
import java.math.RoundingMode

class PortfolioEntryDTO(
    val id: Long? = null,
    val stockSymbol: String,
    val quantity: Double,
    val stock: StockDTO? = null,
    val totalValue: BigDecimal = BigDecimal.ZERO,
    val totalInvestAmount: BigDecimal = BigDecimal.ZERO,
    val profitAndLoss: BigDecimal = BigDecimal.ZERO,
    val profitAndLossPercent: Double = 0.0,
) {
    companion object {
        fun mapToDto(
            portfolioEntry: PortfolioEntry,
            stock: StockDTO,
        ): PortfolioEntryDTO {
            val totalValue = calculateTotalValue(stock.latestQuote.currentPrice, portfolioEntry)
            val profitAndLoss = calculateProfitAndLoss(portfolioEntry, stock.latestQuote.currentPrice)
            val profitAndLossPercent = calculateProfitAndLossPercent(profitAndLoss, portfolioEntry.totalInvestAmount)

            return PortfolioEntryDTO(
                id = portfolioEntry.id,
                stockSymbol = portfolioEntry.stockSymbol,
                quantity = portfolioEntry.quantity,
                stock = stock,
                totalInvestAmount = portfolioEntry.totalInvestAmount,
                totalValue = totalValue,
                profitAndLoss = profitAndLoss,
                profitAndLossPercent = profitAndLossPercent
            )
        }

        fun mapToDtoWithoutStock(portfolioEntry: PortfolioEntry, latestStockQuote: StockQuote): PortfolioEntryDTO {
            val totalValue = calculateTotalValue(latestStockQuote.currentPrice, portfolioEntry)
            val profitAndLoss = calculateProfitAndLoss(portfolioEntry, latestStockQuote.currentPrice)
            val profitAndLossPercent = calculateProfitAndLossPercent(profitAndLoss, portfolioEntry.totalInvestAmount)

            return PortfolioEntryDTO(
                id = portfolioEntry.id,
                stockSymbol = portfolioEntry.stockSymbol,
                quantity = portfolioEntry.quantity,
                totalInvestAmount = portfolioEntry.totalInvestAmount,
                totalValue = totalValue,
                profitAndLoss = profitAndLoss,
                profitAndLossPercent = profitAndLossPercent
            )
        }

        private fun calculateTotalValue(currentQuotePrice: BigDecimal, portfolioEntry: PortfolioEntry): BigDecimal {
            return currentQuotePrice.multiply(portfolioEntry.quantity.toBigDecimal())
        }

        private fun calculateProfitAndLoss(portfolioEntry: PortfolioEntry, currentQuotePrice: BigDecimal): BigDecimal {
            val totalValue = calculateTotalValue(currentQuotePrice, portfolioEntry)
            return totalValue.subtract(portfolioEntry.totalInvestAmount)
        }

        private fun calculateProfitAndLossPercent(
            profitAndLoss: BigDecimal,
            totalInvestAmount: BigDecimal
        ): Double {
            if (totalInvestAmount == BigDecimal.ZERO) return 0.0
            return profitAndLoss.divide(totalInvestAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).toDouble()
        }
    }
}
