package de.thm.mni.backend.smtp.dto

import java.time.LocalDateTime

data class ExternalMailDTO(
    val id: String,
    val folder: String,
    val subject: String,
    val from: String,
    val to: List<String>,
    val cc: List<String>,
    val replyTo: List<String>,
    val content: String,
    val attachmentNames: List<String>,
    val receivedAt: LocalDateTime?,
    val sentAt: LocalDateTime?,
)
