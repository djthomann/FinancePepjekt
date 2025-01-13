package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("finance_owner")
data class Owner(
    @Id
    val id: Long,
    val name: String,
    val mail: String
)