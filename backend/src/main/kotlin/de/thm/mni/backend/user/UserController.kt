package de.thm.mni.backend.user

import de.thm.mni.backend.error.DefaultApiErrors
import de.thm.mni.backend.error.ResourceAlreadyExistsException
import de.thm.mni.backend.error.ResourceNotFoundException
import de.thm.mni.backend.user.dto.UserDTO
import de.thm.mni.backend.user.dto.UserUpdate
import de.thm.mni.backend.user.dto.toDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "User", description = "Manage user accounts — retrieve, update, and delete users.")
@DefaultApiErrors
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(
        operationId = "getAllUsers",
        summary = "List all users",
        description = "Returns all registered users.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Users retrieved successfully.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = UserDTO::class)),
            ),
        ],
    )
    @GetMapping
    fun getAllUsers(): List<UserDTO> = userService.getAllUsers().map { it -> it.toDTO() }

    @Operation(
        operationId = "getCurrentUser",
        summary = "Get current user",
        description = "Returns the application profile linked to the authenticated Keycloak subject.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Current user profile returned.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserDTO::class))],
    )
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): UserDTO = userService.getOrCreateUser(jwt).toDTO()

    @Operation(
        operationId = "getUserById",
        summary = "Get user by ID",
        description = "Returns a single user by their UUID.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "User found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserDTO::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: UUID,
    ): UserDTO? = userService.getUserById(id)?.toDTO()

    @Operation(
        operationId = "updateUser",
        summary = "Update user profile",
        description = "Updates the authenticated user's first name, last name, and email. Only the owner may update their own account.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "User updated successfully.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserDTO::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "409",
        description = "Email is already in use by another account.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody userData: UserUpdate,
        @AuthenticationPrincipal jwt: Jwt,
    ): UserDTO? {
        val currentUser = userService.getOrCreateUser(jwt)
        val existingUser = userService.getUserById(id) ?: throw ResourceNotFoundException("User not found")

        if (existingUser.id != currentUser.id) {
            throw ResourceNotFoundException("User not found")
        }

        val userWithExistingEmail = userService.getUserByEmail(userData.email)

        if (userWithExistingEmail != null && userWithExistingEmail.id != existingUser.id) {
            throw ResourceAlreadyExistsException("Email is already in use by another user")
        }

        val updatedUser =
            User(
                firstName = userData.firstName,
                lastName = userData.lastName,
                email = userData.email,
                externalSubject = existingUser.externalSubject,
            )
        updatedUser.id = existingUser.id

        return userService.updateUser(id, updatedUser).toDTO()
    }

    @Operation(
        operationId = "deleteUser",
        summary = "Delete user account",
        description = "Permanently deletes the authenticated user's own account.",
    )
    @ApiResponse(responseCode = "204", description = "User deleted successfully.")
    @ApiResponse(
        responseCode = "404",
        description = "User not found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @PathVariable id: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        val currentUser = userService.getOrCreateUser(jwt)
        val existingUser = userService.getUserById(id) ?: throw ResourceNotFoundException("User not found")
        if (existingUser.id != currentUser.id) {
            throw ResourceNotFoundException("User not found")
        }
        userService.deleteUser(id)
    }
}
