package de.thm.mni.backend.mail.dto

import de.thm.mni.backend.attachment.dto.AttachmentDTO
import de.thm.mni.backend.mail.enums.MailSource
import de.thm.mni.backend.mail.enums.MailStatus
import de.thm.mni.backend.user.dto.UserDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "Mail data returned by the API.")
data class MailDTO(
    @field:Schema(description = "Unique mail identifier.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    val id: UUID?,
    @field:Schema(description = "The user who created and sent this mail.")
    val sender: UserDTO,
    @field:Schema(description = "Mail subject.", example = "Project Update")
    val subject: String,
    @field:Schema(description = "Mail body text.", example = "Hi, here is the latest project status...")
    val content: String,
    @field:Schema(description = "Current status of the mail (CREATED, SENT, RECEIVED).")
    val status: MailStatus,
    @field:Schema(description = "Origin of the mail (INTERNAL or EXTERNAL).")
    val source: MailSource,
    @field:Schema(description = "TO recipients.")
    val to: List<UserDTO>,
    @field:Schema(description = "CC recipients.")
    val cc: List<UserDTO>,
    @field:Schema(description = "BCC recipients.")
    val bcc: List<UserDTO>,
    @field:Schema(description = "Reply-To recipients.")
    val replyTo: List<UserDTO>,
    @field:Schema(description = "Optional external SMTP recipient address.", example = "external@example.com")
    val externalRecipientAddress: String?,
    @field:Schema(description = "Uploaded file attachments.")
    val attachments: List<AttachmentDTO>,
    @field:Schema(description = "Support ticket number, present on incoming external mails.", example = "TKT-20240101-001")
    val ticketNumber: String?,
    @field:Schema(description = "Timestamp when the mail was created.")
    val createdAt: LocalDateTime,
    @field:Schema(description = "Timestamp of the last update.")
    val updatedAt: LocalDateTime,
    @field:Schema(description = "Timestamp when the mail was sent. Null for drafts.")
    val sentAt: LocalDateTime?,
)
