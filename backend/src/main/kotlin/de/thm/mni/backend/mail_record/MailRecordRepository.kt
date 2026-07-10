package de.thm.mni.backend.mail_record

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface MailRecordRepository : CrudRepository<MailRecord, UUID> {
    fun findMailRecordByMailId(mailId: UUID): MutableList<MailRecord>

    fun findAllByUserId(userId: UUID): MutableList<MailRecord>

    fun deleteById(id: MailRecordId)
}
