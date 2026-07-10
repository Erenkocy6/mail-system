package de.thm.mni.backend.user

import de.thm.mni.backend.error.ResourceAlreadyExistsException
import jakarta.transaction.Transactional
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun createUser(user: User): User = userRepository.save(user)

    fun getUserById(id: UUID): User? = userRepository.findById(id).orElse(null)

    fun getUserByExternalSubject(externalSubject: String): User? = userRepository.findByExternalSubject(externalSubject)

    fun existsUserByEmail(email: String): Boolean = userRepository.existsUserByEmail(email)

    fun getUserByEmail(email: String): User? = userRepository.findUserByEmail(email)

    fun getAllUsers(): List<User> = userRepository.findAll().toList()

    fun updateUser(
        id: UUID,
        updatedUser: User,
    ): User = userRepository.save(updatedUser)

    fun deleteUser(id: UUID) {
        userRepository.deleteById(id)
    }

    @Transactional
    fun getOrCreateUser(jwt: Jwt): User {
        val subject = jwt.subject
        require(subject.isNotBlank()) { "OIDC subject claim must not be blank" }

        getUserByExternalSubject(subject)?.let { return it }

        val email =
            jwt.getClaimAsString("email")
                ?: jwt.getClaimAsString("preferred_username")?.takeIf { it.contains("@") }
                ?: "$subject@keycloak.local"

        val existingByEmail = getUserByEmail(email)
        if (existingByEmail != null) {
            if (existingByEmail.externalSubject != null && existingByEmail.externalSubject != subject) {
                throw ResourceAlreadyExistsException("Email is already linked to another identity.")
            }
            existingByEmail.externalSubject = subject
            fillMissingProfileData(existingByEmail, jwt)
            return userRepository.save(existingByEmail)
        }

        return userRepository.save(
            User(
                firstName = claimOrFallback(jwt, "given_name", "preferred_username", "name", fallback = "Keycloak"),
                lastName = claimOrFallback(jwt, "family_name", fallback = "User"),
                email = email,
                externalSubject = subject,
            ),
        )
    }

    private fun fillMissingProfileData(
        user: User,
        jwt: Jwt,
    ) {
        if (user.firstName.isBlank()) {
            user.firstName = claimOrFallback(jwt, "given_name", "preferred_username", "name", fallback = "Keycloak")
        }
        if (user.lastName.isBlank()) {
            user.lastName = claimOrFallback(jwt, "family_name", fallback = "User")
        }
    }

    private fun claimOrFallback(
        jwt: Jwt,
        vararg names: String,
        fallback: String,
    ): String =
        names
            .asSequence()
            .mapNotNull { name -> jwt.getClaimAsString(name)?.trim()?.takeIf { it.isNotBlank() } }
            .firstOrNull()
            ?: fallback
}
