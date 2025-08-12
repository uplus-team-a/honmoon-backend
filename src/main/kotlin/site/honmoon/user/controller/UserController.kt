package site.honmoon.user.controller

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
import site.honmoon.user.dto.UpdateUserRequest
import site.honmoon.user.dto.UpdateProfileImageRequest
import site.honmoon.user.dto.UserResponse
import site.honmoon.user.service.UserService
import java.util.*

@Tag(name = "사용자", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(
        summary = "특정 사용자 조회",
        description = "사용자 ID(UUID)로 사용자 프로필을 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(schema = Schema(implementation = UserResponse::class))]
        )]
    )
    @GetMapping("/{userId}")
    fun getUser(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
    ): Response<UserResponse> {
        return Response.success(userService.getUser(userId))
    }

    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 사용자의 프로필을 조회합니다. Authorization: Bearer 토큰 필요.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me")
    fun getMe(@CurrentUser currentUser: UserPrincipal): Response<UserResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.getUser(userId))
    }

    @Operation(
        summary = "사용자 포인트 현황 조회",
        description = "특정 사용자의 현재 보유 포인트와 누적 획득/사용 포인트를 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/{userId}/points")
    fun getUserPoints(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
    ): Response<Map<String, Int>> {
        return Response.success(userService.getUserPoints(userId))
    }

    @Operation(
        summary = "내 포인트 현황 조회",
        description = "현재 로그인한 사용자의 포인트 현황을 조회합니다. Authorization: Bearer 필요.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/points")
    fun getMyPoints(@CurrentUser currentUser: UserPrincipal): Response<Map<String, Int>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.getUserPoints(userId))
    }

    @Operation(
        summary = "사용자 퀴즈 통계 조회",
        description = "특정 사용자의 퀴즈 활동 통계를 조회합니다. (총 퀴즈 수, 정답 수, 정확도 등)",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/{userId}/quiz-stats")
    fun getUserQuizStats(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
    ): Response<Map<String, Any>> {
        return Response.success(userService.getUserQuizStats(userId))
    }

    @Operation(
        summary = "내 퀴즈 통계 조회",
        description = "현재 로그인한 사용자의 퀴즈 활동 통계를 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/quiz-stats")
    fun getMyQuizStats(@CurrentUser currentUser: UserPrincipal): Response<Map<String, Any>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.getUserQuizStats(userId))
    }

    @Operation(
        summary = "사용자 미션 통계 조회",
        description = "특정 사용자의 미션 달성 통계를 조회합니다. (총 미션 수, 완료 수, 완료율 등)",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/{userId}/mission-stats")
    fun getUserMissionStats(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
    ): Response<Map<String, Any>> {
        return Response.success(userService.getUserMissionStats(userId))
    }

    @Operation(
        summary = "내 미션 통계 조회",
        description = "현재 로그인한 사용자의 미션 달성 통계를 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/mission-stats")
    fun getMyMissionStats(@CurrentUser currentUser: UserPrincipal): Response<Map<String, Any>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.getUserMissionStats(userId))
    }

    @Operation(
        summary = "프로필 이미지 업데이트",
        description = "특정 사용자의 프로필 이미지를 변경합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(schema = Schema(implementation = UserResponse::class))]
        )]
    )
    @PutMapping("/{userId}/profile-image")
    fun updateUserProfileImage(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "프로필 이미지 URL",
            required = true,
            content = [Content(schema = Schema(implementation = UpdateProfileImageRequest::class))]
        )
        @RequestBody body: UpdateProfileImageRequest,
    ): Response<UserResponse> {
        return Response.success(userService.updateProfileImage(userId, body.imageUrl))
    }

    @Operation(
        summary = "내 프로필 이미지 업데이트",
        description = "현재 로그인한 사용자의 프로필 이미지를 변경합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PutMapping("/me/profile-image")
    fun updateMyProfileImage(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "프로필 이미지 URL",
            required = true,
            content = [Content(schema = Schema(implementation = UpdateProfileImageRequest::class))]
        )
        @RequestBody body: UpdateProfileImageRequest,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<UserResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.updateProfileImage(userId, body.imageUrl))
    }

    @Operation(
        summary = "사용자 프로필 업데이트",
        description = "특정 사용자의 닉네임/이미지를 수정합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PatchMapping("/{userId}")
    fun updateUserProfile(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 사용자 정보",
            required = true,
            content = [Content(schema = Schema(implementation = UpdateUserRequest::class))]
        )
        @RequestBody request: UpdateUserRequest,
    ): Response<UserResponse> {
        return Response.success(userService.updateProfile(userId, request.nickname, request.profileImageUrl))
    }

    @Operation(
        summary = "내 프로필 업데이트",
        description = "현재 로그인한 사용자의 닉네임/이미지를 수정합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PatchMapping("/me")
    fun updateMyProfile(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 사용자 정보",
            required = true,
            content = [Content(schema = Schema(implementation = UpdateUserRequest::class))]
        )
        @RequestBody request: UpdateUserRequest,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<UserResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.updateProfile(userId, request.nickname, request.profileImageUrl))
    }
}