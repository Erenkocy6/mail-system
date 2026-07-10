package de.thm.mni.backend.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
class SecurityConfig(
    @Value("\${springdoc.api-docs.path:/v3/api-docs}") private val apiDocsPath: String,
    @Value("\${springdoc.swagger-ui.path:/swagger-ui}") private val swaggerUiPath: String,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .cors {
                it.configurationSource {
                    CorsConfiguration().apply {
                        allowedOriginPatterns = listOf("http://localhost:*", "http://127.0.0.1:*")
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        allowedHeaders = listOf("*")
                        allowCredentials = true
                        maxAge = 3600L
                    }
                }
            }.sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.authorizeHttpRequests {
                it
                    // Preflight Requests erlauben
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // OpenAPI / Swagger erlauben
                    .requestMatchers(*springDocPaths())
                    .permitAll()
                    // Error Route erlauben
                    .requestMatchers("/error")
                    .permitAll()
                    // Alles andere braucht Keycloak Bearer Auth
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }.formLogin { it.disable() }
            .httpBasic { it.disable() }
            .build()

    private fun springDocPaths(): Array<String> {
        val apiDocs = normalizedPath(apiDocsPath)
        val swaggerUi = normalizedPath(swaggerUiPath)

        return arrayOf(
            // Standard Springdoc OpenAPI
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            // Falls dein API Prefix /v1 ist
            "/v1/v3/api-docs",
            "/v1/v3/api-docs/**",
            "/v1/v3/api-docs.yaml",
            // Falls dein Context Path /api ist und danach /v1 kommt
            "/api/v1/v3/api-docs",
            "/api/v1/v3/api-docs/**",
            "/api/v1/v3/api-docs.yaml",
            // Standard Swagger UI
            "/swagger-ui",
            "/swagger-ui/**",
            "/swagger-ui.html",
            // Swagger UI mit /v1 Prefix
            "/v1/swagger-ui",
            "/v1/swagger-ui/**",
            "/v1/swagger-ui.html",
            // Swagger UI mit /api/v1 Prefix
            "/api/v1/swagger-ui",
            "/api/v1/swagger-ui/**",
            "/api/v1/swagger-ui.html",
            // Werte aus application.properties / application.yml
            apiDocs,
            "$apiDocs/**",
            "$apiDocs.yaml",
            swaggerUi,
            "$swaggerUi/**",
            "$swaggerUi.html",
        )
    }

    private fun normalizedPath(path: String): String {
        val pathWithLeadingSlash =
            if (path.startsWith("/")) path else "/$path"

        return pathWithLeadingSlash.trimEnd('/')
    }
}
