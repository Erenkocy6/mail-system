package de.thm.mni.backend.attachment.dto

import de.thm.mni.backend.attachment.Attachment
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Metadata for an uploaded file attachment.")
data class AttachmentDTO(
    @field:Schema(description = "File size in bytes.", example = "204800")
    val size: Long,
    @field:Schema(description = "Original filename.", example = "report.pdf")
    val fileName: String?,
    @field:Schema(description = "MIME type of the file.", example = "application/pdf")
    val mimeType: String?,
    @field:Schema(description = "Server-side path used to retrieve the file via the /images endpoint.", example = "uploads/abc123.pdf")
    val path: String,
)

fun Attachment.toDTO() =
    AttachmentDTO(
        fileName = this.fileName,
        size = this.size,
        mimeType = this.mimeType,
        path = this.path,
    )
