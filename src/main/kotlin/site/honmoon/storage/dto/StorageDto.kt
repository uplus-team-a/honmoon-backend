package site.honmoon.storage.dto

import java.time.LocalDateTime


data class FileInfo(
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val contentType: String,
    val uploadedAt: LocalDateTime,
)

data class FileListResponse(
    val files: List<FileInfo>,
    val totalCount: Int,
)

data class PresignedUrlResponse(
    val uploadUrl: String,
    val fileName: String,
    val expiresAt: LocalDateTime,
) 
