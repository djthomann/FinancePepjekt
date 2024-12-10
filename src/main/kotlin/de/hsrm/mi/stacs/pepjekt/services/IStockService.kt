package de.hsrm.mi.stacs.pepjekt.services

import de.hsrm.mi.stacs.pepjekt.entities.Quote
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import java.math.BigDecimal
import java.time.LocalDateTime

interface IStockService {

    fun getStockBySymbol(symbol: String): Stock

    fun calculateAveragePrice(symbol: String, from: LocalDateTime, to: LocalDateTime): BigDecimal

    fun getStockHistory(symbol: String): List<Quote>

    fun getStockHistory(symbol: String, from: LocalDateTime, to: LocalDateTime): List<Quote>

}