package de.thm.mni.backend.smtp

import de.thm.mni.backend.error.DefaultApiErrors
import de.thm.mni.backend.smtp.dto.ExternalMailDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "MailGateway", description = "Access external mail via the optional IMAP gateway.")
@DefaultApiErrors
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/mail-gateway")
class MailGatewayController(
    private val imapService: IMAPService,
) {
    @Operation(
        operationId = "getIncomingExternalMail",
        summary = "Fetch external inbox",
        description = "Returns the most recent messages from the configured IMAP mailbox. Requires IMAP gateway to be enabled.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "External mails fetched successfully.",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = ArraySchema(schema = Schema(implementation = ExternalMailDTO::class)),
            ),
        ],
    )
    @GetMapping("/incoming")
    fun getIncomingExternalMail(
        @RequestParam(defaultValue = "20") limit: Int,
    ): List<ExternalMailDTO> = imapService.fetchInbox(limit)
}
