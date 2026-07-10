package de.thm.mni.backend.mail

import de.thm.mni.backend.error.DefaultApiErrors
import de.thm.mni.backend.error.ResourceCannotBeModifiedException
import de.thm.mni.backend.error.ResourceNotFoundException
import de.thm.mni.backend.mail.dto.MailDTO
import de.thm.mni.backend.mail.dto.MailRequest
import de.thm.mni.backend.mail.dto.SupportReplyRequest
import de.thm.mni.backend.mail.dto.toMailCreate
import de.thm.mni.backend.mail.dto.toMailUpdate
import de.thm.mni.backend.mail.enums.MailStatus
import de.thm.mni.backend.mail_record.MailRecordService
import de.thm.mni.backend.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Tag(name = "Mail", description = "Manage mails — inbox, drafts, sent mail, attachments, and support replies.")
@DefaultApiErrors
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/mails")
class MailController(
    private val mailService: MailService,
    private val userService: UserService,
    private val mailRecordService: MailRecordService,
    private val mailMapper: MailMapper,
) {
    @Operation(
        operationId = "getDrafts",
        summary = "List draft mails",
        description = "Returns all draft mails created by the authenticated user.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Drafts retrieved successfully.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = MailDTO::class)),
            ),
        ],
    )
    @GetMapping("/drafts")
    fun getCreatedMails(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<MailDTO> {
        val user = currentUser(jwt)
        val userMails = mailService.getAllCreatedUserMails(user)
        return userMails.map { mail -> mailMapper.toDTO(user, mail) }
    }

    @Operation(
        operationId = "getSentMails",
        summary = "List sent mails",
        description = "Returns all mails sent by the authenticated user.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Sent mails retrieved successfully.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = MailDTO::class)),
            ),
        ],
    )
    @GetMapping("/sent")
    fun getSentMails(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<MailDTO> {
        val user = currentUser(jwt)
        val userMails = mailService.getAllSentUserMails(user)
        return userMails.map { mail -> mailMapper.toDTO(user, mail) }
    }

    @Operation(
        operationId = "createDraft",
        summary = "Create a draft mail",
        description = "Creates a new draft mail with optional file attachments.",
    )
    @ApiResponse(
        responseCode = "201",
        description = "Draft created successfully.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = MailDTO::class))],
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createMail(
        @Valid @RequestPart("data") data: MailRequest,
        @RequestPart("attachments") attachments: List<MultipartFile>,
        @AuthenticationPrincipal jwt: Jwt,
    ): MailDTO {
        val user = currentUser(jwt)

        val createdMail = mailService.createMail(data.toMailCreate(), user, attachments)
        return mailMapper.toDTO(user, createdMail)
    }

    @Operation(
        operationId = "getInbox",
        summary = "List inbox mails",
        description = "Returns all incoming mails for the authenticated user, including shared support mails.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Inbox retrieved successfully.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = MailDTO::class)),
            ),
        ],
    )
    @GetMapping("/incoming")
    fun getIncomingMailsForUser(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<MailDTO> {
        val user = currentUser(jwt)
        val userId = user.id ?: throw ResourceNotFoundException("User not found")
        val userMails =
            (
                mailService.getAllSupportIncomingMails() +
                    mailRecordService.getAllIncomingMailsForUser(userId)
            ).distinctBy { mail -> mail.id }
                .sortedByDescending { mail -> mail.createdAt }
        return userMails.map { mail -> mailMapper.toDTO(user, mail) }
    }

    @Operation(
        operationId = "getMailById",
        summary = "Get mail by ID",
        description = "Returns a single mail by ID. The caller must be the sender, a recipient, or it must be a shared support mail.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Mail found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = MailDTO::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Mail not found or not accessible.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/{mailId}")
    fun getMailById(
        @PathVariable mailId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): MailDTO {
        val user = currentUser(jwt)
        val mail = mailService.getMailById(mailId) ?: throw ResourceNotFoundException("Mail not found")
        val records = mailRecordService.getMailRecordByMailId(mail.id!!)

        val isSharedSupportMail = mail.status == MailStatus.RECEIVED
        if (!isSharedSupportMail && records.none { it.user!!.id == user.id } && mail.sender?.id != user.id) {
            throw ResourceNotFoundException("Mail not found")
        }
        return mailMapper.toDTO(user, mail)
    }

    @Operation(
        operationId = "updateDraft",
        summary = "Update a draft mail",
        description = "Updates an existing draft mail. Only the sender may update their own drafts. Sent mails cannot be modified.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Draft updated successfully.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = MailDTO::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Mail not found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "409",
        description = "Mail cannot be modified — it has already been sent.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @PutMapping("/{mailId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateMail(
        @PathVariable mailId: UUID,
        @Valid @RequestPart("data") mail: MailRequest,
        @RequestPart("attachments") attachments: List<MultipartFile>,
        @AuthenticationPrincipal jwt: Jwt,
    ): MailDTO {
        val user = currentUser(jwt)
        val userId = user.id ?: throw ResourceNotFoundException("User not found")
        val existingMail = mailService.getMailById(mailId) ?: throw ResourceNotFoundException("Mail not found")

        if (existingMail.sender?.id != userId) {
            throw ResourceNotFoundException("Mail not found")
        }

        if (existingMail.status == MailStatus.SENT) {
            throw ResourceCannotBeModifiedException("Cannot update a sent mail")
        }

        val updatedMail = mailService.updateMail(mailId, mail.toMailUpdate(), attachments)
        return mailMapper.toDTO(user, updatedMail)
    }

    @Operation(
        operationId = "deleteMail",
        summary = "Delete a mail",
        description = "Permanently deletes a mail. Only the sender may delete their own mails.",
    )
    @ApiResponse(responseCode = "204", description = "Mail deleted successfully.")
    @ApiResponse(
        responseCode = "404",
        description = "Mail not found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @DeleteMapping("/{mailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMail(
        @PathVariable mailId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        val userId = currentUser(jwt).id ?: throw ResourceNotFoundException("User not found")
        val existingMail = mailService.getMailById(mailId) ?: throw ResourceNotFoundException("Mail not found")
        if (existingMail.sender?.id != userId) {
            throw ResourceNotFoundException("Mail not found")
        }
        mailService.deleteMail(existingMail)
    }

    @Operation(
        operationId = "sendMail",
        summary = "Send an existing draft",
        description = "Sends a previously created draft mail by its ID.",
    )
    @ApiResponse(responseCode = "200", description = "Mail sent successfully.")
    @ApiResponse(
        responseCode = "404",
        description = "Mail not found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @PostMapping("/send/{mailId}")
    fun sendMail(
        @PathVariable mailId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        val userId = currentUser(jwt).id ?: throw ResourceNotFoundException("User not found")
        val existingMail = mailService.getMailById(mailId) ?: throw ResourceNotFoundException("Mail not found")
        if (existingMail.sender?.id != userId) {
            throw ResourceNotFoundException("Mail not found")
        }
        mailService.sendMail(existingMail)
    }

    @Operation(
        operationId = "createAndSendMail",
        summary = "Create and send a mail in one step",
        description = "Creates a new mail and immediately sends it without saving a draft first.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Mail created and sent successfully.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = MailDTO::class))],
    )
    @PostMapping("/send")
    fun createAndSendMail(
        @Valid @RequestPart("data") data: MailRequest,
        @RequestPart("attachments") attachments: List<MultipartFile>,
        @AuthenticationPrincipal jwt: Jwt,
    ): MailDTO {
        val user = currentUser(jwt)

        val createdMail = mailService.createAndSendMail(data.toMailCreate(), user, attachments)
        return mailMapper.toDTO(user, createdMail)
    }

    @Operation(
        operationId = "replyToSupportMail",
        summary = "Reply to a support mail",
        description = "Creates and sends a reply to an incoming support mail. The reply subject includes the original ticket number.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Reply sent successfully.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = MailDTO::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Mail not found.",
        content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ProblemDetail::class))],
    )
    @PostMapping("/{mailId}/reply", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun replyToSupportMail(
        @PathVariable mailId: UUID,
        @Valid @RequestPart("data") data: SupportReplyRequest,
        @RequestPart("attachments") attachments: List<MultipartFile>,
        @AuthenticationPrincipal jwt: Jwt,
    ): MailDTO {
        val responder = currentUser(jwt)
        val reply = mailService.createAndSendSupportReply(mailId, responder, data.content, attachments)
        return mailMapper.toDTO(responder, reply)
    }

    private fun currentUser(jwt: Jwt) = userService.getOrCreateUser(jwt)
}
