package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.ROUNDING_NUMBER_TO_DECIMAL_PLACE
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
                totalInvestAmount = portfolioEntry.totalInvestAmount.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
                totalValue = totalValue.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
                profitAndLoss = profitAndLoss.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
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
                totalInvestAmount = portfolioEntry.totalInvestAmount.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
                totalValue = totalValue.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
                profitAndLoss = profitAndLoss.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
                profitAndLossPercent = profitAndLossPercent
            )
        }

        /**
         * Calculates the total value of a portfolio entry based on the current quote price.
         *
         * This method multiplies the current price of the stock by the quantity of the stock
         * held in the portfolio entry to compute the total value.
         *
         * @param currentQuotePrice the current price of the stock
         * @param portfolioEntry the portfolio entry containing stock quantity
         * @return the total value of the portfolioEntry
         */
        private fun calculateTotalValue(currentQuotePrice: BigDecimal, portfolioEntry: PortfolioEntry): BigDecimal {
            return currentQuotePrice.multiply(portfolioEntry.quantity.toBigDecimal())
        }

        /**
         * Calculates the profit or loss for a portfolio entry.
         *
         * This method computes the difference between the total value of the portfolio entry
         * and the total investment amount to determine the profit or loss.
         *
         * @param portfolioEntry the portfolio entry to calculate profit and loss for
         * @param currentQuotePrice the current price of the stock as a BigDecimal
         * @return the profit or loss as a BigDecimal
         */
        private fun calculateProfitAndLoss(portfolioEntry: PortfolioEntry, currentQuotePrice: BigDecimal): BigDecimal {
            val totalValue = calculateTotalValue(currentQuotePrice, portfolioEntry)
            return totalValue.subtract(portfolioEntry.totalInvestAmount)
        }


        /**
         * Calculates the profit and loss percentage.
         *
         * This method determines the percentage of profit or loss relative to the total investment amount.
         * If the total investment amount is zero, it returns 0.0 to prevent division by zero.
         *
         * @param profitAndLoss the profit or loss amount
         * @param totalInvestAmount the total investment amount
         * @return the profit and loss percentage
         */
        private fun calculateProfitAndLossPercent(
            profitAndLoss: BigDecimal,
            totalInvestAmount: BigDecimal
        ): Double {
            if (totalInvestAmount == BigDecimal.ZERO) return 0.0
            return profitAndLoss.divide(totalInvestAmount, ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).toDouble()
        }
    }
}
