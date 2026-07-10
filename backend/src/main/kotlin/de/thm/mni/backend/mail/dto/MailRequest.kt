package de.thm.mni.backend.mail.dto

import de.thm.mni.backend.mail.validation.AtLeastOneRecipient
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import java.util.UUID

@Schema(description = "Request payload for creating or updating a mail.")
@AtLeastOneRecipient
data class MailRequest(
    @field:Schema(description = "Mail subject. Between 1 and 20 characters.", example = "Project Update")
    @field:Size(min = 1, max = 20, message = "Subject must be between 1 and 20 characters")
    val subject: String,
    @field:Schema(description = "Mail body. Between 1 and 500 characters.", example = "Hi, here is the latest project status...")
    @field:Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
    val content: String,
    @field:Schema(description = "UUIDs of internal TO recipients.")
    val toIds: MutableList<UUID>,
    @field:Schema(description = "UUIDs of internal CC recipients.")
    val ccIds: MutableList<UUID>,
    @field:Schema(description = "UUIDs of internal BCC recipients.")
    val bccIds: MutableList<UUID>,
    @field:Schema(description = "UUIDs of internal Reply-To recipients.")
    val replyToIds: MutableList<UUID>,
    @field:Schema(description = "Optional external email address for SMTP delivery.", example = "external@example.com")
    @field:Email(message = "External recipient email should be valid")
    val externalRecipientAddress: String? = null,
)

fun MailRequest.toMailCreate(): MailCreate =
    MailCreate(
        subject = this.subject,
        content = this.content,
        toIds = this.toIds,
        ccIds = this.ccIds,
        bccIds = this.bccIds,
        replyToIds = this.replyToIds,
        externalRecipientAddress = this.externalRecipientAddress,
    )

fun MailRequest.toMailUpdate(): MailUpdate =
    MailUpdate(
        subject = this.subject,
        content = this.content,
        toIds = this.toIds,
        ccIds = this.ccIds,
        bccIds = this.bccIds,
        replyToIds = this.replyToIds,
        externalRecipientAddress = this.externalRecipientAddress,
    )
