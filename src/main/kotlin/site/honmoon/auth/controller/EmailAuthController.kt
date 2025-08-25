package site.honmoon.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

import org.springframework.web.bind.annotation.*
import site.honmoon.auth.dto.*
import site.honmoon.auth.service.EmailAuthService
import site.honmoon.common.Response

@Tag(name = "이메일 인증", description = "이메일 기반 회원가입/로그인 API")
@RestController
@RequestMapping("/api/auth")
class EmailAuthController(
    private val emailAuthService: EmailAuthService,
) {

    @Operation(
        summary = "회원가입 이메일 발송",
        description = "회원가입을 위한 인증 이메일을 발송합니다. 이메일만 필요하며, 닉네임은 이메일 앞부분으로 자동 설정됩니다. callbackUrl을 지정하면 해당 URL에 토큰 파라미터가 추가됩니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 가입된 이메일)")
        ]
    )
    @PostMapping("/signup/email")
    fun sendSignupEmail(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 요청 정보",
            required = true,
            content = [Content(schema = Schema(implementation = EmailAuthRequest::class))]
        )
        @RequestBody request: EmailAuthRequest,
        @io.swagger.v3.oas.annotations.Parameter(
            description = "인증 완료 후 리다이렉트될 URL (기본값: https://www.honmoon.site/auth/verify)",
            example = "https://localhost:3000/auth/verify"
        )
        @RequestParam(value = "callbackUrl", required = false) callbackUrl: String?
    ): Response<EmailAuthResponse> {
        return Response.success(emailAuthService.sendSignupEmail(request, callbackUrl))
    }

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "성공 - 사용자 ID 반환"),
            ApiResponse(responseCode = "404", description = "등록되지 않은 이메일"),
            ApiResponse(responseCode = "400", description = "비활성화된 계정 또는 잘못된 비밀번호")
        ]
    )
    @PostMapping("/login")
    fun login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 요청 정보",
            required = true,
            content = [Content(schema = Schema(implementation = LoginRequest::class))]
        )
        @RequestBody request: LoginRequest
    ): Response<LoginResponse> {
        return Response.success(emailAuthService.login(request))
    }

    @Operation(
        summary = "인증 토큰 검증 (회원가입 완료)",
        description = "이메일로 받은 인증 토큰을 검증하고 회원가입을 완료합니다. 닉네임과 비밀번호는 필수입니다. 성공시 사용자 ID를 반환하며, 이 ID로 Basic Auth 인증이 가능합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "성공 - 사용자 ID 반환"),
            ApiResponse(responseCode = "404", description = "유효하지 않거나 이미 사용된 토큰"),
            ApiResponse(responseCode = "400", description = "만료된 토큰 또는 필수 필드 누락")
        ]
    )
    @PostMapping("/verify")
    fun verifyToken(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "토큰 검증 요청",
            required = true,
            content = [Content(schema = Schema(implementation = TokenVerifyRequest::class))]
        )
        @RequestBody request: TokenVerifyRequest
    ): Response<TokenVerifyResponse> {
        return Response.success(emailAuthService.verifyToken(request))
    }


}
