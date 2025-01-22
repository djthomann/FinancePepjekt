package de.hsrm.mi.stacs.pepjekt.entities.dtos

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
            val totalProfitAndLossPercent = calculateProfitAndLossPercent(totalProfitAndLoss, totalInvestAmount)

            return InvestmentAccountDTO(
                id = investmentAccountId,
                bankAccount = bankAccount,
                owner = owner,
                portfolio = portfolioEntryDTOs,
                totalValue = totalValue,
                totalProfitAndLoss = totalProfitAndLoss,
                totalProfitAndLossPercent = totalProfitAndLossPercent
            )
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