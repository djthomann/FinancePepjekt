package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("quote")
data class Quote(
    @Id
    var id: Long? = null,
    var value: BigDecimal,
    var timeStamp: LocalDateTime,
    var stock: Stock
)
