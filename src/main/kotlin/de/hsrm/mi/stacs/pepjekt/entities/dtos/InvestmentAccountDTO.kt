package de.hsrm.mi.stacs.pepjekt.entities.dtos

import org.springframework.data.annotation.Id

class InvestmentAccountDTO (
    @Id
    val id: Long? = null,
    val bankAccountId: Long? = null,
    val portfolio: List<PortfolioEntryDTO> = emptyList(),
    val userId: Long? = null,
    val bankAccount: BankAccountDTO,
    val user: UserDTO
)