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
            user: UserDTO,
            stockService: IStockService
        ): InvestmentAccountDTO {
            return InvestmentAccountDTO(
                id = investmentAccount.id,
                bankAccountId = investmentAccount.bankAccountId,
                portfolio = investmentAccount.portfolio.map {
                    PortfolioEntryDTO.mapToDto(
                        it,
                        stock = StockDTO.mapToDto(
                            stockService.getStockBySymbol(it.stockSymbol).block()!!,
                            currentValue = stockService.getCurrentValueBySymbol(it.stockSymbol).block()!!,
                            change = stockService.getChangeBySymbol(it.stockSymbol).block()!!,
                            changePercentage = stockService.getChangePercentageBySymbol(it.stockSymbol).block()!!,
                        )
                    )
                },
                userId = investmentAccount.userId,
                bankAccount = bankAccount,
                owner = user
            )
        }
    }
}