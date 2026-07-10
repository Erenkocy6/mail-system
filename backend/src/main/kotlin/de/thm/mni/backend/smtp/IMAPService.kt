package de.thm.mni.backend.smtp

import de.thm.mni.backend.smtp.dto.ExternalMailDTO
import de.thm.mni.backend.smtp.dto.IncomingAttachmentDTO
import de.thm.mni.backend.smtp.dto.IncomingMailDTO
import jakarta.mail.Address
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.Session
import jakarta.mail.UIDFolder
import jakarta.mail.internet.InternetAddress
import jakarta.mail.search.FlagTerm
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Properties

@Repository
class IMAPService(
    private val mailGatewayProperties: MailGatewayProperties,
) {
    fun fetchInbox(limit: Int? = null): List<ExternalMailDTO> {
        val imap = mailGatewayProperties.imap
        if (!imap.enabled || imap.host.isBlank()) {
            return emptyList()
        }

        val protocol = if (imap.sslEnabled) "imaps" else "imap"
        val fetchLimit = (limit ?: imap.fetchLimit).coerceAtLeast(1)
        val store = createSession(protocol).getStore(protocol)
        store.connect(imap.host, imap.port, imap.username, imap.password)

        val folder = store.getFolder(imap.folder)
        folder.open(Folder.READ_ONLY)

        try {
            if (folder.messageCount == 0) {
                return emptyList()
            }

            val end = folder.messageCount
            val start = maxOf(1, end - fetchLimit + 1)
            val messages = folder.getMessages(start, end).toList().reversed()

            return messages.map { message -> message.toExternalMailDTO(folder.fullName, folder) }
        } finally {
            folder.close(false)
            store.close()
        }
    }

    fun downloadUnreadMessages(onDownloaded: (IncomingMailDTO) -> Boolean): Int {
        val imap = mailGatewayProperties.imap
        if (!imap.enabled || imap.host.isBlank()) {
            return 0
        }

        val protocol = if (imap.sslEnabled) "imaps" else "imap"
        val store = createSession(protocol).getStore(protocol)
        store.connect(imap.host, imap.port, imap.username, imap.password)

        val folder = store.getFolder(imap.folder)
        folder.open(Folder.READ_WRITE)

        try {
            val unreadMessages =
                folder
                    .search(FlagTerm(Flags(Flags.Flag.SEEN), false))
                    .toList()
                    .take(imap.fetchLimit.coerceAtLeast(1))
            var importedCount = 0

            unreadMessages.forEach { message ->
                if (onDownloaded(message.toIncomingMailDTO(folder))) {
                    importedCount += 1
                }
                message.setFlag(Flags.Flag.SEEN, true)
            }

            return importedCount
        } finally {
            folder.close(true)
            store.close()
        }
    }

    private fun createSession(protocol: String): Session {
        val imap = mailGatewayProperties.imap
        return Session.getInstance(
            Properties().apply {
                put("mail.store.protocol", protocol)
                put("mail.$protocol.connectiontimeout", imap.connectionTimeoutMillis.toString())
                put("mail.$protocol.timeout", imap.readTimeoutMillis.toString())
                if (imap.sslEnabled) {
                    put("mail.$protocol.ssl.enable", "true")
                }
            },
        )
    }

    private fun Message.toExternalMailDTO(
        folderName: String,
        folder: Folder,
    ): ExternalMailDTO {
        val attachmentNames = mutableListOf<String>()
        collectAttachmentNames(this, attachmentNames)

        return ExternalMailDTO(
            id = resolveId(this, folder),
            folder = folderName,
            subject = subject ?: "(no subject)",
            from = from.toAddressStrings().firstOrNull() ?: "(unknown sender)",
            to = getRecipients(Message.RecipientType.TO).toAddressStrings(),
            cc = getRecipients(Message.RecipientType.CC).toAddressStrings(),
            replyTo = replyTo.toAddressStrings(),
            content = extractText(this),
            attachmentNames = attachmentNames,
            receivedAt = receivedDate?.toLocalDateTime(),
            sentAt = sentDate?.toLocalDateTime(),
        )
    }

    private fun Message.toIncomingMailDTO(folder: Folder): IncomingMailDTO {
        val sender = from.firstMailboxAddress()
        val replyAddress = replyTo.firstMailboxAddress()

        return IncomingMailDTO(
            messageKey = "${folder.fullName}:${resolveId(this, folder)}",
            subject = subject ?: "(no subject)",
            senderAddress = sender?.address ?: "",
            senderName = sender?.personal,
            replyToAddress = replyAddress?.address ?: sender?.address ?: "",
            content = extractText(this),
            attachments = collectAttachments(this),
        )
    }

    private fun resolveId(
        message: Message,
        folder: Folder,
    ): String =
        if (folder is UIDFolder) {
            folder.getUID(message).toString()
        } else {
            message.messageNumber.toString()
        }

    private fun Array<Address>?.toAddressStrings(): List<String> {
        val addresses = this ?: return emptyList()

        return addresses
            .mapNotNull { address ->
                when (address) {
                    is InternetAddress -> {
                        if (address.personal.isNullOrBlank()) {
                            address.address
                        } else {
                            "${address.personal} <${address.address}>"
                        }
                    }

                    else -> address.toString()
                }
            }.filter { it.isNotBlank() }
    }

    private fun Array<Address>?.firstMailboxAddress(): InternetAddress? =
        this
            ?.asSequence()
            ?.mapNotNull { address -> address as? InternetAddress }
            ?.firstOrNull { address -> !address.address.isNullOrBlank() }

    private fun extractText(part: Part): String =
        when {
            part.isAttachment() -> ""
            part.isMimeType("text/plain") ->
                part.content
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            part.isMimeType("text/html") ->
                part.content
                    ?.toString()
                    ?.stripHtml()
                    .orEmpty()
            part.isMimeType("multipart/*") -> {
                val multipart = part.content as Multipart
                val texts = mutableListOf<String>()
                for (index in 0 until multipart.count) {
                    val bodyPart = multipart.getBodyPart(index)
                    val text = extractText(bodyPart)
                    if (text.isNotBlank()) {
                        texts.add(text)
                    }
                }
                texts.firstOrNull().orEmpty()
            }

            else -> ""
        }

    private fun collectAttachments(part: Part): List<IncomingAttachmentDTO> {
        val attachments = mutableListOf<IncomingAttachmentDTO>()
        collectAttachments(part, attachments)
        return attachments
    }

    private fun collectAttachments(
        part: Part,
        collector: MutableList<IncomingAttachmentDTO>,
    ) {
        when {
            part.isMimeType("multipart/*") -> {
                val multipart = part.content as Multipart
                for (index in 0 until multipart.count) {
                    collectAttachments(multipart.getBodyPart(index), collector)
                }
            }

            part.isAttachment() -> {
                collector +=
                    IncomingAttachmentDTO(
                        fileName = part.fileName ?: "attachment",
                        mimeType = part.contentType,
                        content = part.inputStream.use { inputStream -> inputStream.readBytes() },
                    )
            }
        }
    }

    private fun collectAttachmentNames(
        part: Part,
        collector: MutableList<String>,
    ) {
        when {
            part.isMimeType("multipart/*") -> {
                val multipart = part.content as Multipart
                for (index in 0 until multipart.count) {
                    collectAttachmentNames(multipart.getBodyPart(index), collector)
                }
            }

            part.isAttachment() -> {
                collector += part.fileName ?: "attachment"
            }
        }
    }

    private fun Part.isAttachment(): Boolean = Part.ATTACHMENT.equals(disposition, ignoreCase = true) || fileName != null

    private fun java.util.Date.toLocalDateTime(): LocalDateTime =
        toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

    private fun String.stripHtml(): String =
        replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
