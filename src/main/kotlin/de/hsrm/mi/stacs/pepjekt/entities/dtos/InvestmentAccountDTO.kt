package de.hsrm.mi.stacs.pepjekt.entities.dtos

import org.springframework.data.annotation.Id
import java.math.BigDecimal

class InvestmentAccountDTO (
    @Id
    val id: Long? = null,
    val bankAccountId: Long? = null,
    val portfolio: List<PortfolioEntryDTO> = emptyList(),
    val totalValue: BigDecimal,
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO
)