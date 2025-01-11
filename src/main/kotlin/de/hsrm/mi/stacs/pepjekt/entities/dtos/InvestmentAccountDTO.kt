package de.hsrm.mi.stacs.pepjekt.entities.dtos

import org.springframework.data.annotation.Id

class InvestmentAccountDTO (
    @Id
    val id: Long? = null,
    val bankAccountId: Long? = null,
    val portfolio: List<PortfolioEntryDTO> = emptyList(),
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO
)