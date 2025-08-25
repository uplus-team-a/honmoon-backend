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
        private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5MB
    }

    @Value("\${GCP_BUCKET_NAME:}")
    private lateinit var bucketName: String

    @Value("\${GCP_PROJECT_ID:}")
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
        userId: String,
        contentType: String? = null,
        maxSizeBytes: Long = MAX_FILE_SIZE_BYTES,
    ): PresignedUrlResponse {
        val fileExtension = getFileExtensionFromContentType(contentType)
        val uniqueFileName = generateUniqueFileName(fileExtension)
        val fullPath = "images/uploaded/$userId/$uniqueFileName"

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
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withExtHeaders(mapOf(
                "Content-Length" to maxSizeBytes.toString()
            ))
        )

        val publicUrl = getPublicUrl(uniqueFileName, "images/uploaded/$userId")

        logger.info { "Presigned URL 생성: $uniqueFileName, 사용자: $userId, 최대크기: ${maxSizeBytes / 1024 / 1024}MB, 만료시간: $expiresAt" }

        return PresignedUrlResponse(
            uploadUrl = url.toString(),
            fileName = uniqueFileName,
            publicUrl = publicUrl,
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

    /**
     * 공개 URL 생성 (프로필 이미지 등)
     */
    fun getPublicUrl(fileName: String, folder: String = "uploads"): String {
        val fullPath = "$folder/$fileName"
        return "https://storage.googleapis.com/$bucketName/$fullPath"
    }

    private fun getFileExtension(fileName: String): String {
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".")
        } else {
            ""
        }
    }

    private fun generateUniqueFileName(extension: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().replace("-", "")

        return if (extension.isNotEmpty()) {
            "${uuid}_${timestamp}.${extension}"
        } else {
            "${uuid}_${timestamp}"
        }
    }

    private fun getFileExtensionFromContentType(contentType: String?): String {
        return when (contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
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

    /**
     * 파일 크기가 제한을 초과하는지 확인
     */
    fun isFileSizeValid(fileSizeBytes: Long, maxSizeBytes: Long = MAX_FILE_SIZE_BYTES): Boolean {
        return fileSizeBytes <= maxSizeBytes
    }

    /**
     * 업로드된 파일의 실제 크기 확인
     */
    fun getUploadedFileSize(fileName: String, folder: String = "images"): Long? {
        val fullPath = "$folder/$fileName"
        val blobId = BlobId.of(bucketName, fullPath)
        val blob = storage.get(blobId)
        return blob?.size
    }
} 
