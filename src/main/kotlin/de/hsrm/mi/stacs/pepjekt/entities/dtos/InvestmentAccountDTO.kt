package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.ROUNDING_NUMBER_TO_DECIMAL_PLACE
import java.math.BigDecimal
import java.math.RoundingMode

class InvestmentAccountDTO(
    val id: Long? = null,
    val portfolio: List<PortfolioEntryDTO> = emptyList(),
    val totalValue: BigDecimal,
    val bankAccount: BankAccountDTO,
    val totalProfitAndLoss: BigDecimal = BigDecimal.ZERO,
    val totalProfitAndLossPercent: Double = 0.0,
    val owner: OwnerDTO
) {
    companion object {
        fun mapToDto(
            investmentAccountId: Long,
            bankAccount: BankAccountDTO,
            owner: OwnerDTO,
            portfolioEntryDTOs: List<PortfolioEntryDTO>,
            totalValue: BigDecimal
        ): InvestmentAccountDTO {
            val totalInvestAmount = portfolioEntryDTOs.sumOf { it.totalInvestAmount }
            val totalProfitAndLoss = portfolioEntryDTOs.sumOf { it.profitAndLoss }
            val totalProfitAndLossPercent = calculateTotalProfitAndLossPercent(totalProfitAndLoss, totalInvestAmount)

            return InvestmentAccountDTO(
                id = investmentAccountId,
                bankAccount = bankAccount,
                owner = owner,
                portfolio = portfolioEntryDTOs,
                totalValue = totalValue.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
                totalProfitAndLoss = totalProfitAndLoss.setScale(ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP),
                totalProfitAndLossPercent = totalProfitAndLossPercent
            )
        }

        /**
         * Calculates the profit and loss percentage of the investmentaccount.
         *
         * This method takes the profit and loss value of all portfolioEntries the total investment amount as inputs,
         * and computes the percentage of profit or loss relative to the total investment.
         * If the total investment amount is zero, it returns 0.0 to avoid division by zero.
         *
         * @param profitAndLoss the profit or loss amount of all portfolioEntries
         * @param totalInvestAmount the total investment amount of all portfolioEntries
         * @return the profit and loss percentage of the whole investmentaccount
         */
        private fun calculateTotalProfitAndLossPercent(
            profitAndLoss: BigDecimal,
            totalInvestAmount: BigDecimal
        ): Double {
            if (totalInvestAmount == BigDecimal.ZERO) return 0.0
            return profitAndLoss.divide(totalInvestAmount, ROUNDING_NUMBER_TO_DECIMAL_PLACE, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).toDouble()
        }
    }
}