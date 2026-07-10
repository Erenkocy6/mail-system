package de.thm.mni.backend.mail

import de.thm.mni.backend.mail.enums.MailStatus
import de.thm.mni.backend.user.User
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface MailRepository : CrudRepository<Mail, UUID> {
    fun findAllBySender(sender: User): MutableList<Mail>

    fun findAllByStatusOrderByCreatedAtDesc(status: MailStatus): MutableList<Mail>

    fun existsByExternalMessageKey(externalMessageKey: String): Boolean
}
