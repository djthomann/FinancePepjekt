package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import org.springframework.data.annotation.*
import java.math.BigDecimal

class StockDetailsDTO(
    @Id
    val symbol: String,
    val description: String,
    val figi: String,
    val currency: Currency,
    val currentValue: BigDecimal = BigDecimal.ZERO,
    val change: BigDecimal = BigDecimal.ZERO,
    val changePercentage: BigDecimal = BigDecimal.ZERO,
    val amount: BigDecimal = BigDecimal.ZERO
) {
    companion object {
        fun mapToDto(
            stock: Stock,
            currentValue: BigDecimal,
            change: BigDecimal,
            changePercentage: BigDecimal,
            amount: BigDecimal,
        ): StockDetailsDTO {
            return StockDetailsDTO(
                symbol = stock.symbol,
                description = stock.description,
                figi = stock.figi,
                currency = stock.currency,
                currentValue = currentValue,
                change = change,
                changePercentage = changePercentage,
                amount = amount
            )
        }
    }
}