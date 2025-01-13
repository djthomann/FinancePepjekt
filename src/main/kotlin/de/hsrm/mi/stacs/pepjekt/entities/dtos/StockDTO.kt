package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Currency
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import org.springframework.data.annotation.*
import java.math.BigDecimal

class StockDTO(
    val symbol: String,
    val description: String,
    val name: String,
    val figi: String,
    val currency: Currency,
    /*
    val currentValue: number,
    val change: number,
    val changePercentage: number
     */
) {
    companion object {
        fun mapToDto(
            stock: Stock,
        ): StockDTO {
            return StockDTO(
                symbol = stock.symbol,
                description = stock.description,
                figi = stock.figi,
                currency = stock.currency,
                name = stock.name,
            )
        }
    }
}