package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Owner
import org.springframework.data.annotation.Id

data class OwnerDTO(
    val id: Long,
    val name: String,
    val mail: String
) {
    companion object {
        fun mapToDto(
            owner: Owner
        ): OwnerDTO {
            return OwnerDTO(
                id = owner.id,
                name = owner.name,
                mail = owner.mail
            )
        }
    }
}