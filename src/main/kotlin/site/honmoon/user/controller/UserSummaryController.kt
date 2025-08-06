package site.honmoon.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.user.dto.UserSummaryResponse
import site.honmoon.user.service.UserSummaryService
import java.util.*

@Tag(name = "User Summary", description = "사용자 요약 정보 관련 API")
@RestController
@RequestMapping("/api/user-summary")
class UserSummaryController(
    private val userSummaryService: UserSummaryService
) {

    @Operation(
        summary = "사용자 요약 정보 조회",
        description = "사용자의 포인트, 퀴즈 활동, 미션 달성률 등 요약 정보를 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserSummaryResponse::class),
                examples = [ExampleObject(
                    name = "success",
                    value = "{\n  \"userId\": \"123e4567-e89b-12d3-a456-426614174000\",\n  \"totalPoints\": 1200,\n  \"totalActivities\": 15\n}"
                )]
            )]
        )]
    )
    @GetMapping("/{userId}")
    fun getUserSummary(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<UserSummaryResponse> {
        return Response.success(userSummaryService.getUserSummary(userId))
    }

    @Operation(
        summary = "내 요약 정보 조회",
        description = "현재 로그인한 사용자의 요약 정보를 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me")
    fun getMySummary(
        @CurrentUser currentUser: UserPrincipal
    ): Response<UserSummaryResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userSummaryService.getUserSummary(userId))
    }

    @Operation(
        summary = "사용자 포인트 현황 조회",
        description = "사용자의 현재 보유 포인트, 누적 포인트, 사용 포인트 정보를 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = Map::class),
                examples = [ExampleObject(name = "success", value = "{\\n  \\\"current\\\": 500, \\\"total\\\": 1200, \\\"used\\\": 700\\n}")]
            )]
        )]
    )
    @GetMapping("/{userId}/points")
    fun getUserPoints(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<Map<String, Int>> {
        return Response.success(userSummaryService.getUserPoints(userId))
    }

    @Operation(
        summary = "내 포인트 현황 조회",
        description = "현재 로그인한 사용자의 포인트 현황을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/points")
    fun getMyPoints(
        @CurrentUser currentUser: UserPrincipal
    ): Response<Map<String, Int>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userSummaryService.getUserPoints(userId))
    }

    @Operation(
        summary = "사용자 퀴즈 활동 통계 조회",
        description = "사용자의 퀴즈 참여 횟수, 정답률 등 퀴즈 활동 통계를 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/{userId}/quiz-stats")
    fun getUserQuizStats(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<Map<String, Any>> {
        return Response.success(userSummaryService.getUserQuizStats(userId))
    }

    @Operation(
        summary = "내 퀴즈 활동 통계 조회",
        description = "현재 로그인한 사용자의 퀴즈 통계를 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/quiz-stats")
    fun getMyQuizStats(
        @CurrentUser currentUser: UserPrincipal
    ): Response<Map<String, Any>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userSummaryService.getUserQuizStats(userId))
    }

    @Operation(
        summary = "사용자 미션 달성률 조회",
        description = "사용자의 미션 완료율과 관련 통계를 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/{userId}/mission-stats")
    fun getUserMissionStats(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<Map<String, Any>> {
        return Response.success(userSummaryService.getUserMissionStats(userId))
    }

    @Operation(
        summary = "내 미션 달성률 조회",
        description = "현재 로그인한 사용자의 미션 달성률을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/mission-stats")
    fun getMyMissionStats(
        @CurrentUser currentUser: UserPrincipal
    ): Response<Map<String, Any>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userSummaryService.getUserMissionStats(userId))
    }
} 