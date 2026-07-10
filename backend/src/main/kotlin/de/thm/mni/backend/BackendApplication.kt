package de.thm.mni.backend

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@OpenAPIDefinition(
    info =
        Info(
            title = "THM Mail System API",
            version = "1.0.0",
            description =
                "REST API for the THM Web Technologies Mail System. " +
                    "Provides authenticated mail workflows, draft management, file attachments, " +
                    "and an optional SMTP/IMAP gateway for external support mail.",
            contact =
                Contact(
                    name = "THM Web Technologies",
                    email = "Andrej.Sajenko@mni.thm.de",
                ),
            license =
                License(
                    name = "MIT",
                    url = "https://opensource.org/licenses/MIT",
                ),
        ),
    servers = [
        Server(url = "http://localhost:8080/api", description = "Local development"),
        Server(url = "http://localhost:8081/api", description = "Docker Compose"),
    ],
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Keycloak OpenID Connect access token. Pass as: Authorization: Bearer <access_token>",
)
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
