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
        private const val PRESIGNED_URL_EXPIRATION_MINUTES = 60L
        private const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L // 10MB로 증가
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
     * Presigned URL 생성 (업로드용)
     * GCP Storage PUT 메서드로 올바른 서명 생성
     */
    fun generatePresignedUploadUrl(
        userId: String,
        contentType: String? = null,
        maxSizeBytes: Long = MAX_FILE_SIZE_BYTES,
    ): PresignedUrlResponse {
        val fileExtension = getFileExtensionFromContentType(contentType)
        val uniqueFileName = generateSimpleFileName(fileExtension)
        val fullPath = "images/uploaded/$userId/$uniqueFileName"

        val blobId = BlobId.of(bucketName, fullPath)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(contentType ?: getContentType(fileExtension))
            .build()

        val expiresAt = LocalDateTime.now().plusMinutes(PRESIGNED_URL_EXPIRATION_MINUTES)

        // GCP Storage PUT 메서드 presigned URL 생성
        // V4 서명 방식 사용하여 올바른 서명 생성
        val url = storage.signUrl(
            blobInfo,
            PRESIGNED_URL_EXPIRATION_MINUTES,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withV4Signature()
        )

        val publicUrl = getPublicUrl(uniqueFileName, "images/uploaded/$userId")

        logger.info { "PUT Presigned URL 생성: $uniqueFileName, 사용자: $userId, 만료시간: $expiresAt" }

        return PresignedUrlResponse(
            uploadUrl = url.toString(),
            fileName = uniqueFileName,
            publicUrl = publicUrl,
            expiresAt = expiresAt,
            maxFileSizeMB = (maxSizeBytes / 1024 / 1024).toInt()
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
            TimeUnit.MINUTES
            // V4 서명 제거하여 안정성 향상
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

    /**
     * 간단한 파일명 생성 (10글자 + 시간값)
     */
    fun generateSimpleFileName(extension: String): String {
        val timestamp = System.currentTimeMillis()
        val randomString = generateRandomString(10)
        
        return if (extension.isNotEmpty()) {
            "${randomString}_${timestamp}.${extension}"
        } else {
            "${randomString}_${timestamp}"
        }
    }

    /**
     * 10글자 랜덤 문자열 생성
     */
    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
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
