package site.honmoon.storage.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.common.Response
import site.honmoon.storage.dto.PresignedUrlResponse
import site.honmoon.storage.entity.FileMetadata
import site.honmoon.storage.repository.FileMetadataRepository
import site.honmoon.storage.service.GcpStorageService
import site.honmoon.storage.service.UploadRateLimitService
import java.util.*

@Tag(name = "File", description = "File Upload/Download API")
@RestController
@RequestMapping("/api/v1/files")
class StorageController(
    private val gcpStorageService: GcpStorageService,
    private val uploadRateLimitService: UploadRateLimitService,
    private val fileMetadataRepository: FileMetadataRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Operation(summary = "파일 업로드 URL 생성", description = "파일 업로드를 위한 Presigned URL을 생성합니다.")
    @PostMapping("/upload-url")
    fun createUploadUrl(
        @Parameter(description = "원본 파일명") @RequestParam("fileName") fileName: String,
        @Parameter(description = "저장할 폴더명 (기본값: images)") @RequestParam(
            "folder",
            defaultValue = "images"
        ) folder: String,
        @Parameter(description = "Content-Type (선택사항)") @RequestParam(
            "contentType",
            required = false
        ) contentType: String?,
        @site.honmoon.auth.security.CurrentUser currentUser: site.honmoon.auth.security.UserPrincipal?,
        @Parameter(description = "사용자 ID") @RequestParam("userId") userId: UUID,
    ): Response<PresignedUrlResponse> {
        logger.info { "파일 업로드 URL 생성 요청: $fileName by $userId" }

        if (!uploadRateLimitService.canUpload(userId.toString())) {
            logger.warn { "사용자 업로드 제한 초과: $userId" }
            return Response.error("업로드 제한에 도달했습니다. 잠시 후 다시 시도해주세요.")
        }

        val result = gcpStorageService.generatePresignedUploadUrl(fileName, folder, contentType)
        uploadRateLimitService.recordUpload(userId.toString())
        
        // 파일 메타데이터 저장
        val fileMetadata = FileMetadata(
            userId = userId,
            fileName = result.fileName,
            originalName = fileName,
            fileUrl = "https://storage.googleapis.com/honmoon-bucket/$folder/${result.fileName}",
            contentType = contentType,
            folder = folder,
            createdBy = userId.toString(),
            modifiedBy = userId.toString()
        )
        fileMetadataRepository.save(fileMetadata)
        
        logger.info { "파일 업로드 URL 생성 완료: ${result.fileName} by $userId" }
        return Response.success(result)
    }

    @Operation(summary = "파일 URL 조회", description = "업로드된 파일의 공개 URL을 조회합니다.")
    @GetMapping("/url/{fileName}")
    fun getFileUrl(
        @Parameter(description = "파일명") @PathVariable fileName: String,
    ): Response<String> {
        logger.info { "파일 URL 조회 요청: $fileName" }
        
        val fileMetadata = fileMetadataRepository.findByFileNameAndIsActiveTrue(fileName)
            ?: return Response.error("파일을 찾을 수 없습니다.")
        
        logger.info { "파일 URL 조회 완료: $fileName" }
        return Response.success(fileMetadata.fileUrl)
    }


    @Operation(summary = "파일 삭제", description = "사용자가 업로드한 파일을 삭제합니다. (본인 파일만 삭제 가능)")
    @DeleteMapping("/{fileName}")
    fun deleteFile(
        @Parameter(description = "삭제할 파일명") @PathVariable fileName: String,
        @Parameter(description = "사용자 ID") @RequestParam("userId") userId: UUID,
    ): Response<Boolean> {
        logger.info { "파일 삭제 요청: $fileName by $userId" }
        
        // 파일 소유권 확인
        val fileMetadata = fileMetadataRepository.findByUserIdAndFileNameAndIsActiveTrue(userId, fileName)
            ?: return Response.error("파일을 찾을 수 없거나 삭제 권한이 없습니다.")
        
        // GCP Storage에서 파일 삭제
        val result = gcpStorageService.deleteFile(fileName, fileMetadata.folder)
        
        if (result) {
            // 메타데이터를 비활성화 (소프트 삭제)
            val updatedMetadata = fileMetadata.copy(
                isActive = false,
                modifiedBy = userId.toString()
            )
            fileMetadataRepository.save(updatedMetadata)
            logger.info { "파일 삭제 완료: $fileName by $userId" }
        } else {
            logger.warn { "파일 삭제 실패: $fileName by $userId" }
        }
        
        return Response.success(result)
    }

} 
