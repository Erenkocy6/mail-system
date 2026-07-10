package de.thm.mni.backend.storage

import de.thm.mni.backend.error.DefaultApiErrors
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths

@Tag(name = "Storage", description = "Serve uploaded attachment files.")
@DefaultApiErrors
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/images")
class StorageController(
    private val fileStorageService: FileStorageService,
) {
    @Operation(
        operationId = "getImage",
        summary = "Download an attachment file",
        description = "Returns the raw file bytes for an uploaded attachment identified by its filename.",
    )
    @ApiResponse(responseCode = "200", description = "File returned successfully.")
    @ApiResponse(responseCode = "404", description = "File not found.")
    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
    ): ResponseEntity<Resource> {
        val file: Resource = fileStorageService.load(filename)

        val contentType = MediaType.parseMediaType(Files.probeContentType(Paths.get(file.uri)))

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(contentType.toString()))
            .body(file)
    }
}
