package de.thm.mni.backend.mail.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Request payload for replying to an incoming support mail.")
data class SupportReplyRequest(
    @field:Schema(
        description = "Reply text. Between 1 and 500 characters.",
        example = "Thank you for contacting us. Your issue has been resolved.",
    )
    @field:Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
    val content: String,
)
