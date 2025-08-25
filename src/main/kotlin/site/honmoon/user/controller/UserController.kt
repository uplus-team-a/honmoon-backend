package site.honmoon.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal

import site.honmoon.common.Response
import site.honmoon.user.dto.UpdateProfileImageRequest
import site.honmoon.user.dto.UpdateProfileRequest
import site.honmoon.user.dto.UpdateUserRequest
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
        description = "현재 로그인한 사용자의 프로필을 조회합니다. Basic 인증 필요 (username: user_id(UUID), password: jiwondev).",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me")
    fun getMe(@CurrentUser currentUser: UserPrincipal): Response<UserResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.getUser(userId))
    }

    @Operation(
        summary = "내 프로필(간략)",
        description = "프로필, 포인트 요약, 최근 활동 10건, 최근 포인트 10건을 반환합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/profile/summary")
    fun getMyProfileSummary(@CurrentUser currentUser: UserPrincipal): Response<site.honmoon.user.dto.UserProfileSummaryResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.getUserProfileSummary(userId))
    }

    @Operation(
        summary = "내 프로필(상세)",
        description = "프로필, 포인트 요약, 전체 활동 리스트, 전체 포인트 내역을 반환합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/profile/detail")
    fun getMyProfileDetail(@CurrentUser currentUser: UserPrincipal): Response<site.honmoon.user.dto.UserProfileDetailResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.getUserProfileDetail(userId))
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

    @Operation(
        summary = "내 프로필 수정",
        description = "현재 로그인한 사용자의 프로필 정보를 수정합니다. (닉네임, 프로필 이미지 URL)",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PutMapping("/me/profile")
    fun updateProfile(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 프로필 정보",
            required = true,
            content = [Content(schema = Schema(implementation = UpdateProfileRequest::class))]
        )
        @RequestBody request: UpdateProfileRequest,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<UserResponse> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(userService.updateProfile(userId, request.nickname, request.profileImageUrl))
    }

}
