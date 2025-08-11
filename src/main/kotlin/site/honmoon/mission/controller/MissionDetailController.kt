package site.honmoon.mission.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.mission.dto.MissionAnswerRequest
import site.honmoon.mission.dto.MissionAnswerResponse
import site.honmoon.mission.dto.MissionDetailResponse
import site.honmoon.mission.dto.MissionImageAnswerRequest
import site.honmoon.mission.service.MissionAnswerService
import site.honmoon.mission.service.MissionDetailService
import site.honmoon.storage.dto.PresignedUrlResponse
import site.honmoon.storage.service.GcpStorageService
import java.util.*

@Tag(name = "Mission Detail", description = "미션 상세 정보 관련 API")
@RestController
@RequestMapping("/api/missions")
class MissionDetailController(
    private val missionDetailService: MissionDetailService,
    private val missionAnswerService: MissionAnswerService,
    private val gcpStorageService: GcpStorageService,
) {
    @Operation(
        summary = "미션 상세 정보 조회",
        description = "특정 미션의 상세 정보를 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(schema = Schema(implementation = MissionDetailResponse::class))]
        )]
    )
    @GetMapping("/{id}")
    fun getMissionDetail(
        @Parameter(description = "미션 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<MissionDetailResponse> {
        return Response.success(missionDetailService.getMissionDetail(id))
    }

    @Operation(
        summary = "미션 답변 제출",
        description = "퀴즈 미션에 텍스트 답변을 제출합니다. 정답이면 포인트가 지급됩니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/{id}/submit-answer")
    fun submitMissionAnswer(
        @Parameter(description = "미션 ID", example = "1")
        @PathVariable id: Long,
        @RequestBody request: MissionAnswerRequest,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<MissionAnswerResponse> {
        val userId = UUID.fromString(currentUser.subject)
        val result = missionAnswerService.submitAnswer(id, request.answer, userId)
        return Response.success(result)
    }

    @Operation(
        summary = "미션 이미지 답변 제출",
        description = "이미지 업로드 퀴즈 미션에 이미지를 제출합니다. 서버가 AI로 텍스트를 추출해 정답을 판별합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/{id}/submit-image-answer")
    fun submitMissionImageAnswer(
        @Parameter(description = "미션 ID", example = "1")
        @PathVariable id: Long,
        @RequestBody request: MissionImageAnswerRequest,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<MissionAnswerResponse> {
        val userId = UUID.fromString(currentUser.subject)
        val result = missionAnswerService.submitAnswerWithImage(id, request.imageUrl, userId)
        return Response.success(result)
    }

    @Operation(
        summary = "미션 이미지 업로드 URL 발급",
        description = "미션 관련 이미지를 버킷에 업로드하기 위한 Presigned URL을 발급합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/{id}/image/upload-url")
    fun createMissionImageUploadUrl(
        @PathVariable id: Long,
        @RequestParam fileName: String,
        @RequestParam(required = false) contentType: String?,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<PresignedUrlResponse> {
        UUID.fromString(currentUser.subject)
        val folder = "missions"
        val result = gcpStorageService.generatePresignedUploadUrl(fileName, folder, contentType)
        return Response.success(result)
    }
} 
