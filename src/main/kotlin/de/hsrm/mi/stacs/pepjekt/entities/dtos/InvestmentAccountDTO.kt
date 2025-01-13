package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import de.hsrm.mi.stacs.pepjekt.services.IStockService
import org.springframework.data.annotation.Id
import java.math.BigDecimal

class InvestmentAccountDTO(
    @Id
    val id: Long? = null,
    val bankAccountId: Long? = null,
    val portfolio: List<PortfolioEntryDTO> = emptyList(),
    val totalValue: BigDecimal,
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO
) {
    companion object {
        fun mapToDto(
            investmentAccount: InvestmentAccount,
            bankAccount: BankAccountDTO,
            owner: OwnerDTO,
            portfolioEntryDTOs: List<PortfolioEntryDTO>,
            totalValue: BigDecimal,
        ): InvestmentAccountDTO {
            return InvestmentAccountDTO(
                id = investmentAccount.id,
                bankAccountId = investmentAccount.bankAccountId,
                bankAccount = bankAccount,
                owner = owner,
                portfolio = portfolioEntryDTOs,
                totalValue = totalValue
            )
        }
    }
}