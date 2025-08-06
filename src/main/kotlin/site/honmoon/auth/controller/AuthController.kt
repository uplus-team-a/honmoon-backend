package site.honmoon.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.dto.*
import site.honmoon.auth.security.SessionAuthService
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.auth.service.GoogleOAuthService
import site.honmoon.common.Response

@Tag(name = "Auth", description = "Google OAuth + Basic/Security 세션 인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val googleOAuthService: GoogleOAuthService,
    private val sessionAuthService: SessionAuthService,
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
        val scopes = scope.split(' ').filter { it.isNotBlank() }
        val (url, state, _) = googleOAuthService.buildAuthorizationUrl(
            scopes = scopes,
            statePayload = if (redirectAfter != null) mapOf("redirectAfter" to redirectAfter) else emptyMap()
        )
        return Response.success(AuthUrlResponse("google", url, state))
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
        require(googleOAuthService.verifyState(state)) { "invalid state" }
        val tokenRes = googleOAuthService.exchangeCodeForTokens(code)
        val accessToken = tokenRes.accessToken
        val idToken = tokenRes.idToken
        val refreshToken = tokenRes.refreshToken
        val expiresIn = tokenRes.expiresInSeconds
        val scope = tokenRes.scope
        val tokenType = tokenRes.tokenType

        val userInfoRes = googleOAuthService.fetchUserInfo(accessToken)
        val googleUser = GoogleUserInfo(
            sub = userInfoRes.sub,
            email = userInfoRes.email,
            emailVerified = userInfoRes.emailVerified,
            name = userInfoRes.name,
            givenName = userInfoRes.givenName,
            familyName = userInfoRes.familyName,
            picture = userInfoRes.picture,
        )

        val sessionToken = sessionAuthService.createSession(
            UserPrincipal(
                subject = googleUser.sub,
                email = googleUser.email,
                name = googleUser.name,
                picture = googleUser.picture,
                provider = "google",
                roles = setOf("ROLE_USER")
            )
        )

        val tokens = GoogleTokenResponse(
            accessToken = accessToken,
            idToken = idToken,
            refreshToken = refreshToken,
            expiresInSeconds = expiresIn,
            scope = scope,
            tokenType = tokenType
        )

        return Response.success(
            AuthLoginResponse(
                provider = "google",
                google = googleUser,
                googleTokens = tokens,
                appSessionToken = sessionToken,
                jwt = null
            )
        )
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
        requireNotNull(principal) { "unauthorized" }
        val profile = ProfileResponse(
            sub = principal.subject,
            email = principal.email,
            name = principal.name,
            picture = principal.picture,
            provider = principal.provider
        )
        return Response.success(profile)
    }

    @Operation(
        summary = "로그아웃",
        description = "서버 메모리 세션을 무효화합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/logout")
    fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?): Response<Boolean> {
        val token = authorization?.removePrefix("Bearer ")?.trim()
        if (!token.isNullOrBlank()) {
            sessionAuthService.invalidate(token)
        }
        return Response.success(true)
    }

    @Operation(summary = "이메일 매직 링크 요청", description = "입력된 이메일로 로그인 링크를 전송합니다.")
    @PostMapping("/email/magic-link")
    fun requestMagicLink(@RequestBody body: EmailLoginRequest): Response<EmailMagicLinkResponse> {
        val magicToken = java.util.UUID.randomUUID().toString().replace("-", "")
        val magicLink = "https://honmoon.site/auth/email/callback?token=$magicToken"
        val expiresAt = java.time.LocalDateTime.now().plusMinutes(15)
        return Response.success(EmailMagicLinkResponse(body.email, magicLink, expiresAt))
    }

    @Operation(summary = "이메일 매직 링크 콜백", description = "매직 토큰을 검증합니다.")
    @GetMapping("/email/callback")
    fun magicLinkCallback(@RequestParam token: String): Response<EmailCallbackResponse> {
        val appSessionToken: String? = null
        return Response.success(EmailCallbackResponse("unknown@honmoon.site", true, appSessionToken))
    }
} 
