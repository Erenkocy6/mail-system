package de.thm.mni.backend.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

@Schema(description = "Request payload for updating user profile information.")
data class UserUpdate(
    @field:Schema(description = "Updated first name.", example = "Max")
    @field:Size(min = 1, message = "First name must not be empty")
    val firstName: String,
    @field:Schema(description = "Updated last name.", example = "Mustermann")
    @field:Size(min = 1, message = "Last name must not be empty")
    val lastName: String,
    @field:Schema(description = "Updated email address. Must be unique.", example = "max.mustermann@example.com")
    @field:Email(message = "Email should be valid")
    val email: String,
)
