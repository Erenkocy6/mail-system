package de.thm.mni.backend.smtp

import de.thm.mni.backend.mail.Mail
import de.thm.mni.backend.mail.enums.MailType
import de.thm.mni.backend.mail_record.MailRecordService
import de.thm.mni.backend.storage.FileStorageService
import jakarta.mail.internet.InternetAddress
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Repository

@Repository
class SMTPService(
    private val mailRecordService: MailRecordService,
    private val fileStorageService: FileStorageService,
    private val mailGatewayProperties: MailGatewayProperties,
    private val javaMailSenderProvider: ObjectProvider<JavaMailSender>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun isEnabled(): Boolean = mailGatewayProperties.smtp.enabled && javaMailSenderProvider.ifAvailable != null

    fun sendEmail(mail: Mail): Boolean {
        val javaMailSender = javaMailSenderProvider.ifAvailable
        if (!mailGatewayProperties.smtp.enabled || javaMailSender == null) {
            logger.warn("SMTP sending is disabled or not configured.")
            return false
        }
        if (!mail.externalRecipientAddress.isNullOrBlank() && mailGatewayProperties.fromAddress.isBlank()) {
            logger.warn("External SMTP recipients require a configured MAIL_FROM_ADDRESS.")
            return false
        }

        val sender = mail.sender ?: return false
        val mailId = mail.id ?: return false
        val records = mailRecordService.getMailRecordByMailId(mailId)

        val toRecipients =
            (
                records
                    .filter { it.type == MailType.TO }
                    .mapNotNull { it.user?.email } + listOfNotNull(mail.externalRecipientAddress)
            ).distinct()
        val ccRecipients =
            records
                .filter { it.type == MailType.CC }
                .mapNotNull { it.user?.email }
                .distinct()
        val bccRecipients =
            records
                .filter { it.type == MailType.BCC }
                .mapNotNull { it.user?.email }
                .distinct()
        val replyToRecipients =
            records
                .filter { it.type == MailType.REPLY_TO }
                .mapNotNull { it.user?.email }
                .distinct()

        if (toRecipients.isEmpty() && ccRecipients.isEmpty() && bccRecipients.isEmpty()) {
            logger.warn("Mail {} has no SMTP recipients and cannot be sent.", mail.id)
            return false
        }

        val fromAddress =
            mailGatewayProperties.fromAddress.ifBlank {
                sender.email
            }

        return runCatching {
            val mimeMessage = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, mail.attachments.isNotEmpty(), Charsets.UTF_8.name())

            helper.setFrom(InternetAddress(fromAddress, "${sender.firstName} ${sender.lastName}"))
            helper.setSubject(mail.subject)
            helper.setText(mail.content, false)

            if (toRecipients.isNotEmpty()) {
                helper.setTo(toRecipients.toTypedArray())
            }
            if (ccRecipients.isNotEmpty()) {
                helper.setCc(ccRecipients.toTypedArray())
            }
            if (bccRecipients.isNotEmpty()) {
                helper.setBcc(bccRecipients.toTypedArray())
            }

            val replyToAddresses =
                if (replyToRecipients.isNotEmpty()) {
                    replyToRecipients
                } else {
                    listOf(fromAddress)
                }
            mimeMessage.replyTo = replyToAddresses.map { InternetAddress(it) }.toTypedArray()
            mimeMessage.sentDate = java.util.Date()

            mail.attachments.forEach { attachment ->
                val fileName = attachment.fileName ?: attachment.path
                helper.addAttachment(fileName, fileStorageService.load(attachment.path))
            }

            javaMailSender.send(mimeMessage)
            true
        }.onFailure { error ->
            logger.error("Failed to send mail {} via SMTP.", mail.id, error)
        }.getOrDefault(false)
    }
}
