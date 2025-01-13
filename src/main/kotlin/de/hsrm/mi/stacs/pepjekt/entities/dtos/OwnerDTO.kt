package de.hsrm.mi.stacs.pepjekt.entities.dtos

import de.hsrm.mi.stacs.pepjekt.entities.Stock
import de.hsrm.mi.stacs.pepjekt.entities.User
import org.springframework.data.annotation.Id
import java.math.BigDecimal

data class OwnerDTO(
    @Id
    val id: Long,
    val name: String,
    val mail: String
){
    companion object {
        fun mapToDto(
            user: User
        ): UserDTO {
            return UserDTO(
                id = user.id,
                name = user.name,
                mail = user.mail
            )
        }
    }
}