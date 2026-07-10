package de.thm.mni.backend.smtp.dto

data class IncomingMailDTO(
    val messageKey: String,
    val subject: String,
    val senderAddress: String,
    val senderName: String?,
    val replyToAddress: String,
    val content: String,
    val attachments: List<IncomingAttachmentDTO>,
)

data class IncomingAttachmentDTO(
    val fileName: String,
    val mimeType: String?,
    val content: ByteArray,
)
