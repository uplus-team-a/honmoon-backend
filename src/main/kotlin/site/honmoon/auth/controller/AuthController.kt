package site.honmoon.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.dto.*
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.auth.service.AuthService
import site.honmoon.common.Response

@Tag(name = "Auth", description = "Google OAuth + Basic/Security 세션 인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @Operation(
        summary = "Google OAuth 인증 URL 생성",
        description = "프론트는 이 URL로 리다이렉트합니다. state는 위변조 방지를 위한 서명 값을 포함합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공",
                content = [Content(schema = Schema(implementation = AuthUrlResponse::class))]
            )
        ]
    )
    @GetMapping("/google/url")
    fun getGoogleAuthUrl(
        @Parameter(description = "스코프 목록 공백 구분", example = "openid email profile")
        @RequestParam(required = false, defaultValue = "profile email openid") scope: String,
        @Parameter(description = "인증 성공 후 프론트에서 이동할 경로", example = "/")
        @RequestParam(required = false) redirectAfter: String?,
    ): Response<AuthUrlResponse> {
        val res = authService.buildGoogleAuthUrl(scope, redirectAfter)
        return Response.success(res)
    }

    @Operation(
        summary = "Google OAuth 콜백",
        description = "구글에서 전달한 code/state로 토큰을 교환하고 사용자 정보를 조회한 뒤, 서버 메모리 세션 토큰을 발급합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공",
                content = [Content(schema = Schema(implementation = AuthLoginResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "state 검증 실패 또는 파라미터 오류"),
        ]
    )
    @GetMapping("/google/callback")
    fun googleCallback(
        @Parameter(description = "구글이 전달하는 인증 코드")
        @RequestParam code: String,
        @Parameter(description = "요청 시 생성된 state")
        @RequestParam state: String,
    ): Response<AuthLoginResponse> {
        val res = authService.handleGoogleCallback(code, state)
        return Response.success(res)
    }

    @Operation(
        summary = "현재 사용자 프로필",
        description = "Basic 인증 또는 Bearer 세션 토큰으로 호출 시 현재 인증된 프로필을 반환합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(schema = Schema(implementation = ProfileResponse::class))]
        )]
    )
    @GetMapping("/me")
    fun me(@site.honmoon.auth.security.CurrentUser principal: UserPrincipal?): Response<ProfileResponse> {
        val res = authService.buildCurrentProfile(principal)
        return Response.success(res)
    }

    @Operation(
        summary = "로그아웃",
        description = "서버 메모리 세션을 무효화합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/logout")
    fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?): Response<LogoutResponse> {
        val res = authService.logout(authorization)
        return Response.success(res)
    }

    @Operation(summary = "이메일 회원가입 링크 전송", description = "입력된 이메일로 회원가입 확인 링크를 전송합니다.")
    @PostMapping("/signup/email")
    fun signupByEmail(@RequestBody body: EmailSignUpRequest): Response<EmailMagicLinkResponse> {
        val res = authService.sendSignupMagicLink(body)
        return Response.success(res)
    }

    @Operation(summary = "이메일 로그인 링크 전송(사용자 ID)", description = "프론트에서 이메일을 입력하지 않고 사용자 ID로 이메일을 조회하여 매직 링크를 전송합니다.")
    @PostMapping("/login/email/by-user")
    fun loginByEmailByUser(@RequestBody body: EmailLoginByUserRequest): Response<EmailMagicLinkResponse> {
        val res = authService.sendLoginMagicLinkByUserId(body)
        return Response.success(res)
    }

    @Operation(summary = "이메일 매직 링크 콜백", description = "매직 토큰을 검증합니다.")
    @GetMapping("/email/callback")
    fun magicLinkCallback(
        @RequestParam token: String,
        @RequestParam(required = false) purpose: String?,
    ): ResponseEntity<Void> {
        return authService.handleMagicLinkCallback(token, purpose)
    }
}
