package de.hsrm.mi.stacs.pepjekt.entities.dtos

import org.springframework.data.annotation.Id

data class UserDTO(
    @Id
    val id: Long,
    val name: String,
    val mail: String
)