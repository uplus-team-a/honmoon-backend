package site.honmoon.storage.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.storage.dto.PresignedUrlResponse
import site.honmoon.storage.service.GcpStorageService

@Tag(name = "이미지 업로드", description = "이미지 업로드 관련 API")
@RestController
@RequestMapping("/api/images")
class ImageUploadController(
    private val gcpStorageService: GcpStorageService,
) {

    @Operation(
        summary = "이미지 업로드 URL 생성",
        description = "이미지 업로드를 위한 Presigned URL과 공개 URL을 함께 생성합니다. 파일명은 서버에서 자동 생성되며, 사용자별 폴더에 저장됩니다. 생성된 URL로 PUT 메서드를 사용해 직접 업로드할 수 있습니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/upload-url")
    fun generateImageUploadUrl(
        @RequestParam(required = false) contentType: String?,
        @RequestParam(required = false, defaultValue = "10") maxSizeMB: Int,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<PresignedUrlResponse> {
        val uploadResponse = gcpStorageService.generatePresignedUploadUrl(
            userId = currentUser.subject,
            contentType = contentType,
            maxSizeBytes = maxSizeMB * 1024L * 1024L
        )
        return Response.success(uploadResponse)
    }

    @Operation(
        summary = "GCP Storage CORS 설정 적용",
        description = "GCP Storage 버킷에 CORS 설정을 적용하여 프론트엔드에서 presigned URL 사용 시 CORS 오류를 해결합니다. 관리자용 엔드포인트입니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/configure-cors")
    fun configureCors(
        @CurrentUser currentUser: UserPrincipal,
    ): Response<String> {
        val result = gcpStorageService.applyCorsConfiguration()
        return Response.success(result)
    }
}
