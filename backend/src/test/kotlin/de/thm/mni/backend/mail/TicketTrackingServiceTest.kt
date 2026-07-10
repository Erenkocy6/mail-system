package de.thm.mni.backend.mail

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TicketTrackingServiceTest {
    private val ticketTrackingService = TicketTrackingService()

    @Test
    fun `prepends a new ticket number to an untracked subject`() {
        val mail = Mail().apply { subject = "Help needed" }

        val trackedSubject = ticketTrackingService.ensureTicketSubject(mail)

        assertTrue(trackedSubject.matches(Regex("""\[TICKET-[A-Z0-9]{8}] Help needed""")))
        assertNotNull(mail.ticketNumber)
    }

    @Test
    fun `reuses an existing ticket number in the subject`() {
        val mail = Mail().apply { subject = "[TICKET-AB12CD34] Existing thread" }

        val trackedSubject = ticketTrackingService.ensureTicketSubject(mail)

        assertEquals("[TICKET-AB12CD34] Existing thread", trackedSubject)
        assertEquals("TICKET-AB12CD34", mail.ticketNumber)
    }
}
