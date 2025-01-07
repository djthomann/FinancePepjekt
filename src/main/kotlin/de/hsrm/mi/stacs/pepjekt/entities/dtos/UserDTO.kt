package de.hsrm.mi.stacs.pepjekt.entities.dtos

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("finance_user")
data class UserDTO(
    @Id
    val id: Long,
    val name: String,
    val mail: String
)