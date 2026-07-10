package de.thm.mni.backend.storage

import de.thm.mni.backend.attachment.dto.AttachmentDTO
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Repository
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Repository
class FileStorageRepository(
    @Value("\${file.upload-dir}") private val uploadDir: String,
) {
    private var rootLocation: Path? = null

    @PostConstruct
    fun init() {
        try {
            this.rootLocation = Paths.get(uploadDir)
            Files.createDirectories(rootLocation)
        } catch (e: IOException) {
            throw RuntimeException("Could not initialize folder for upload!")
        }
    }

    fun saveFile(file: MultipartFile): AttachmentDTO? {
        if (file.isEmpty) {
            return null
        }

        return file.inputStream.use { inputStream ->
            saveFile(
                originalFilename = file.originalFilename ?: "attachment",
                contentType = file.contentType,
                size = file.size,
                inputStream = inputStream,
            )
        }
    }

    fun saveFile(
        fileName: String,
        mimeType: String?,
        content: ByteArray,
    ): AttachmentDTO? {
        if (content.isEmpty()) {
            return null
        }

        return content.inputStream().use { inputStream ->
            saveFile(
                originalFilename = fileName,
                contentType = mimeType,
                size = content.size.toLong(),
                inputStream = inputStream,
            )
        }
    }

    private fun saveFile(
        originalFilename: String,
        contentType: String?,
        size: Long,
        inputStream: InputStream,
    ): AttachmentDTO {
        try {
            val safeFileName = Paths.get(originalFilename).fileName?.toString() ?: "attachment"
            val newFilename = UUID.randomUUID().toString() + safeExtension(safeFileName)
            val destinationFile =
                rootLocation
                    ?.resolve(Paths.get(newFilename))
                    ?.normalize()
                    ?.toAbsolutePath()
                    ?: throw RuntimeException("Could not resolve upload destination.")

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING)

            return AttachmentDTO(
                size = size,
                fileName = safeFileName,
                mimeType = contentType,
                path = newFilename,
            )
        } catch (e: IOException) {
            throw RuntimeException("Failed to store file.", e)
        }
    }

    private fun safeExtension(fileName: String): String {
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
        return extension
            .takeIf { it.matches(Regex("[A-Za-z0-9]{1,10}")) }
            ?.let { ".$it" }
            .orEmpty()
    }

    fun deleteFile(filename: String) {
        val filePath =
            rootLocation?.resolve(filename)
                ?: throw RuntimeException("Could not delete the file!")

        try {
            Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            throw RuntimeException("Could not delete the file!", e)
        }
    }

    fun load(filename: String): Resource {
        try {
            val file = rootLocation?.resolve(filename) ?: throw RuntimeException("File not found!")
            val resource = UrlResource(file.toUri())

            if (resource.exists() || resource.isReadable) {
                return resource
            } else {
                throw RuntimeException("Could not read the file!")
            }
        } catch (e: MalformedURLException) {
            throw RuntimeException("Error: " + e.message)
        }
    }
}
