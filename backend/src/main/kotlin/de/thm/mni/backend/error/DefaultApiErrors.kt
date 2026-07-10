package de.thm.mni.backend.error

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "Bad request — validation error or malformed input.",
    content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ProblemDetail::class),
        ),
    ],
)
@ApiResponse(
    responseCode = "401",
    description = "Unauthorized - missing or invalid Keycloak Bearer access token.",
    content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ProblemDetail::class),
        ),
    ],
)
@ApiResponse(
    responseCode = "500",
    description = "Internal server error.",
    content = [
        Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = Schema(implementation = ProblemDetail::class),
        ),
    ],
)
annotation class DefaultApiErrors
