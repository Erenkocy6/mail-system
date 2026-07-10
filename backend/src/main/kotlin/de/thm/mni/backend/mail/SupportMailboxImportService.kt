package de.thm.mni.backend.mail

import de.thm.mni.backend.smtp.IMAPService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SupportMailboxImportService(
    private val imapService: IMAPService,
    private val mailService: MailService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(
        initialDelayString = "\${mail.gateway.imap.poll-initial-delay-millis:10000}",
        fixedDelayString = "\${mail.gateway.imap.poll-delay-millis:60000}",
    )
    fun importUnreadMessages() {
        val importedCount = imapService.downloadUnreadMessages { mail -> mailService.importSupportMail(mail) }
        if (importedCount > 0) {
            logger.info("Imported {} unread support mail(s) from IMAP.", importedCount)
        }
    }
}
