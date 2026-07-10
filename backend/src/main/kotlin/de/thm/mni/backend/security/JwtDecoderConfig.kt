package de.thm.mni.backend.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class JwtDecoderConfig(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") private val issuerUri: String,
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") private val jwkSetUri: String,
) {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        val decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri))
        return decoder
    }
}
