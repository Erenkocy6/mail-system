package de.thm.mni.backend.mail

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TicketTrackingService {
    private val ticketPattern = Regex("""\[(TICKET-[A-Z0-9]{8})]""", RegexOption.IGNORE_CASE)

    fun ensureTicketSubject(mail: Mail): String {
        val ticketNumber =
            mail.ticketNumber
                ?: ticketPattern
                    .find(mail.subject)
                    ?.groupValues
                    ?.get(1)
                    ?.uppercase()
                ?: newTicketNumber()

        mail.ticketNumber = ticketNumber
        if (!ticketPattern.containsMatchIn(mail.subject)) {
            mail.subject = "[$ticketNumber] ${mail.subject}"
        }

        return mail.subject
    }

    private fun newTicketNumber(): String =
        "TICKET-" +
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(8)
                .uppercase()
}
