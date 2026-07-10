package de.thm.mni.backend.mail

import de.thm.mni.backend.attachment.dto.toDTO
import de.thm.mni.backend.mail.dto.MailDTO
import de.thm.mni.backend.mail.enums.MailSource
import de.thm.mni.backend.mail.enums.MailType
import de.thm.mni.backend.mail_record.MailRecordService
import de.thm.mni.backend.user.User
import de.thm.mni.backend.user.dto.UserDTO
import de.thm.mni.backend.user.dto.toDTO
import org.springframework.stereotype.Component

@Component
class MailMapper(
    private val mailRecordService: MailRecordService,
) {
    fun toDTO(
        user: User,
        mail: Mail,
    ): MailDTO {
        val records = mailRecordService.getMailRecordByMailId(mail.id!!)
        val sender =
            mail.sender?.toDTO()
                ?: UserDTO(
                    id = null,
                    firstName = mail.externalSenderName ?: mail.externalSenderAddress ?: "External",
                    lastName = "",
                    email = mail.externalSenderAddress ?: "",
                )
        val externalRecipient =
            mail.externalRecipientAddress
                ?.takeIf { it.isNotBlank() }
                ?.let { address -> UserDTO(id = null, firstName = address, lastName = "", email = address) }

        return MailDTO(
            id = mail.id,
            sender = sender,
            subject = mail.subject,
            content = mail.content,
            status = mail.status,
            source = if (mail.sender?.id == user.id) MailSource.INTERN else MailSource.EXTERN,
            to = records.filter { it.type == MailType.TO }.map { it.user!!.toDTO() } + listOfNotNull(externalRecipient),
            cc = records.filter { it.type == MailType.CC }.map { it.user!!.toDTO() },
            bcc =
                records.filter { it.type == MailType.BCC && (it.user!!.id == user.id || mail.sender?.id == user.id) }.map {
                    it.user!!
                        .toDTO()
                },
            replyTo = records.filter { it.type == MailType.REPLY_TO }.map { it.user!!.toDTO() },
            externalRecipientAddress = mail.externalRecipientAddress,
            attachments = mail.attachments.map { it -> it.toDTO() },
            ticketNumber = mail.ticketNumber,
            createdAt = mail.createdAt,
            updatedAt = mail.updatedAt,
            sentAt = mail.sentAt,
        )
    }
}
