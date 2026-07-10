package de.thm.mni.backend.user.dto

import de.thm.mni.backend.user.User
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "User profile data returned by the API.")
data class UserDTO(
    @field:Schema(description = "Unique user identifier.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID?,
    @field:Schema(description = "First name.", example = "Max")
    val firstName: String,
    @field:Schema(description = "Last name.", example = "Mustermann")
    val lastName: String,
    @field:Schema(description = "Email address.", example = "max.mustermann@example.com")
    val email: String,
)

fun User.toDTO() =
    UserDTO(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
    )
