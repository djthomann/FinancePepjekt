package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.InvestmentAccount
import org.springframework.data.annotation.Id

class InvestmentAccountDTO (
    @Id
    val id: Long? = null,
    val bankAccountId: Long? = null,
    val portfolio: List<PortfolioEntryDTO> = emptyList(),
    val userId: Long? = null,
    val bankAccount: BankAccountDTO,
    val user: UserDTO
) {
    companion object {
        fun mapToDto(
            investmentAccount: InvestmentAccount,
            bankAccount: BankAccountDTO,
            user: UserDTO
        ): InvestmentAccountDTO {
            return InvestmentAccountDTO(
                id = investmentAccount.id,
                bankAccountId = investmentAccount.bankAccountId,
                portfolio = investmentAccount.portfolio.map { PortfolioEntryDTO.mapToDto(
                    it,
                    stock = TODO(),
                    currentValue = TODO(),
                    amount = TODO(),
                    change = TODO(),
                    changePercentage = TODO(),
                ) },
                userId = investmentAccount.userId,
                bankAccount = bankAccount,
                user = user
            )
        }
    }
}