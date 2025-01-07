package de.hsrm.mi.stacs.pepjekt.entities.dtos

import org.springframework.data.annotation.Id
import java.math.BigDecimal

data class PortfolioEntryDTO(
    @Id
    val id: Long? = null,
    val investmentAccountId: Long,
    val stockSymbol: String,
    val quantity: Double,
    val stock: StockDTO,
    val currentValue: BigDecimal = BigDecimal.ZERO,
    val amount: BigDecimal = BigDecimal.ZERO,
    val change: BigDecimal = BigDecimal.ZERO,
    val changePercentage: BigDecimal = BigDecimal.ZERO
)
