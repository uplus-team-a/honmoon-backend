package site.honmoon.activity.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.activity.dto.UserActivityResponse
import site.honmoon.activity.service.UserActivityService
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import java.util.*

@Tag(name = "User Activity", description = "사용자 활동 관련 API")
@RestController
@RequestMapping("/api/user-activities")
class UserActivityController(
    private val userActivityService: UserActivityService,
) {

    @Operation(
        summary = "사용자 활동 상세 조회",
        description = "특정 사용자 활동의 상세 정보를 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserActivityResponse::class),
                examples = [ExampleObject(
                    name = "success",
                    value = "{\n  \"id\": 1,\n  \"userId\": \"123e4567-e89b-12d3-a456-426614174000\",\n  \"placeId\": 10,\n  \"description\": \"한강공원에서 산책\",\n  \"isCompleted\": true,\n  \"pointsEarned\": 0\n}"
                )]
            )]
        )]
    )
    @GetMapping("/{id}")
    fun getUserActivity(
        @Parameter(description = "사용자 활동 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<UserActivityResponse> {
        return Response.success(userActivityService.getUserActivity(id))
    }

    

    @Operation(
        summary = "내 활동 내역 조회",
        description = "현재 로그인한 사용자의 활동 내역을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me")
    fun getMyActivityHistory(
        @CurrentUser currentUser: UserPrincipal,
    ): Response<List<UserActivityResponse>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userActivityService.getUserActivityHistory(userId))
    }

    @Operation(
        summary = "장소별 활동 내역 조회",
        description = "특정 장소에서 발생한 모든 활동 내역을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/place/{placeId}")
    fun getActivityHistoryByPlace(
        @Parameter(description = "장소 ID", example = "1")
        @PathVariable placeId: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<List<UserActivityResponse>> {
        return Response.success(userActivityService.getActivityHistoryByPlace(placeId))
    }

    @Operation(
        summary = "활동 기록 생성",
        description = "사용자가 특정 장소에서 활동했을 때 활동 내역을 기록합니다. (userId당 placeId는 1개만 허용)",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserActivityResponse::class),
                examples = [ExampleObject(
                    name = "success",
                    value = "{\n  \"placeId\": 10, \n  \"description\": \"한강공원에서 산책\"\n}"
                )]
            )]
        )]
    )
    @PostMapping
    fun createActivity(
        @Parameter(description = "장소 ID", example = "1")
        @RequestParam placeId: Long,
        @Parameter(description = "활동 설명", example = "한강공원에서 산책")
        @RequestParam description: String,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<UserActivityResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userActivityService.createActivity(userId, placeId, description))
    }

    

    @Operation(
        summary = "내 최근 활동 조회",
        description = "현재 로그인한 사용자의 최근 활동 내역을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/recent")
    fun getMyRecentActivity(
        @Parameter(description = "조회할 활동 수", example = "10")
        @RequestParam limit: Int = 10,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<List<UserActivityResponse>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userActivityService.getUserRecentActivity(userId, limit))
    }

    // 퀴즈 제출 엔드포인트는 /api/missions/{id}/submit-quiz(, /me)로 통합됨
} 
