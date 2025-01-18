package de.hsrm.mi.stacs.pepjekt.entities.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import de.hsrm.mi.stacs.pepjekt.entities.OrderType
import de.hsrm.mi.stacs.pepjekt.entities.Stock
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderDTO(
    var id: Long? = null,
    val purchaseAmount: BigDecimal,
    val type: OrderType,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    val executionTimestamp: LocalDateTime,
    val investmentAccountId: Long,
    val stockSymbol: String,
    val stock: Stock
)