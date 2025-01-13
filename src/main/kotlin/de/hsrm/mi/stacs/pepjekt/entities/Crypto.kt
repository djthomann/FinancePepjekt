package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("crypto")
class Crypto(
    @Id
    val symbol: String,
    var name: String,
    var cprice: BigDecimal
)