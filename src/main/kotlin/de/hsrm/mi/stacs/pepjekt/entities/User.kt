package de.hsrm.mi.stacs.pepjekt.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("finance_user")
data class User(
    @Id
    val id: Long,
    val name: String,
    val mail: String
) {
}