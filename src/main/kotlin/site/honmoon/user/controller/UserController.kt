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