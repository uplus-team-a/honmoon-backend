package site.honmoon.storage.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import site.honmoon.storage.dto.PresignedUrlResponse
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException

@Service
class GcpStorageService(
    private val storage: Storage,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val PRESIGNED_URL_EXPIRATION_MINUTES = 15L
    }

    @Value("\${spring.cloud.gcp.storage.bucket}")
    private lateinit var bucketName: String

    @Value("\${spring.cloud.gcp.storage.project-id}")
    private lateinit var projectId: String

    /**
     * 파일 다운로드
     */
    fun downloadFile(fileName: String, folder: String = "uploads"): ByteArray {
        val fullPath = "$folder/$fileName"
        val blobId = BlobId.of(bucketName, fullPath)
        val blob = storage.get(blobId) ?: throw EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, fileName)

        return blob.getContent()
    }

    /**
     * 파일 삭제
     */
    fun deleteFile(fileName: String, folder: String = "uploads"): Boolean {
        val fullPath = "$folder/$fileName"
        val blobId = BlobId.of(bucketName, fullPath)
        return storage.delete(blobId)
    }

    /**
     * Presigned URL 생성 (안전한 업로드를 위한)
     * S3와 유사한 방식으로 GCP에서 직접 업로드 가능한 URL 제공
     */
    fun generatePresignedUploadUrl(
        originalFileName: String,
        folder: String = "uploads",
        contentType: String? = null,
    ): PresignedUrlResponse {
        val fileExtension = getFileExtension(originalFileName)
        val uniqueFileName = generateUniqueFileName(originalFileName, fileExtension)
        val fullPath = "$folder/$uniqueFileName"

        val blobId = BlobId.of(bucketName, fullPath)
        val blobInfoBuilder = BlobInfo.newBuilder(blobId)
            .setContentType(contentType ?: getContentType(fileExtension))

        val blobInfo = blobInfoBuilder.build()

        val expiresAt = LocalDateTime.now().plusMinutes(PRESIGNED_URL_EXPIRATION_MINUTES)

        val url = storage.signUrl(
            blobInfo,
            PRESIGNED_URL_EXPIRATION_MINUTES,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.withV4Signature(),
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT)
        )

        logger.info { "Presigned URL 생성: $uniqueFileName, 만료시간: $expiresAt" }

        return PresignedUrlResponse(
            uploadUrl = url.toString(),
            fileName = uniqueFileName,
            expiresAt = expiresAt
        )
    }

    /**
     * Presigned URL 생성 (다운로드용)
     */
    fun generatePresignedDownloadUrl(
        fileName: String,
        folder: String = "uploads",
        expirationMinutes: Long = 60,
    ): String {
        val fullPath = "$folder/$fileName"
        val blobId = BlobId.of(bucketName, fullPath)
        val blob = storage.get(blobId) ?: throw EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, fileName)

        return storage.signUrl(
            blob,
            expirationMinutes,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.withV4Signature()
        ).toString()
    }

    private fun getFileExtension(fileName: String): String {
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".")
        } else {
            ""
        }
    }

    private fun generateUniqueFileName(originalFileName: String, extension: String): String {
        System.currentTimeMillis()
        UUID.randomUUID().toString().replace("-", "")

        return if (extension.isNotEmpty()) {
            "${'$'}{uuid}_${'$'}{timestamp}.${'$'}{extension}"
        } else {
            "${'$'}{uuid}_${'$'}{timestamp}"
        }
    }

    private fun getContentType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }
} 
