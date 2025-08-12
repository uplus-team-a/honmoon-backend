package site.honmoon.auth.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.auth.dto.*
import site.honmoon.auth.security.SecurityAuthorities
import site.honmoon.auth.security.SessionAuthService
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.AuthException
import site.honmoon.common.exception.InvalidRequestException
import site.honmoon.user.service.UserService
import java.time.Instant
import java.time.LocalDateTime

@Service
class AuthService(
    private val googleOAuthService: GoogleOAuthService,
    private val sessionAuthService: SessionAuthService,
    private val emailService: EmailService,
    private val magicLinkService: MagicLinkService,
    private val userService: UserService,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder,
) {
    private val logger = KotlinLogging.logger {}
    fun buildGoogleAuthUrl(scope: String, redirectAfter: String?, frontendCallbackUrl: String?): AuthUrlResponse {
        val scopes = scope.split(' ').filter { it.isNotBlank() }
        val (url, state, _) = googleOAuthService.buildAuthorizationUrl(
            scopes = scopes,
            statePayload = buildMap {
                if (redirectAfter != null) put("redirectAfter", redirectAfter)
                if (frontendCallbackUrl != null) put("frontendCallbackUrl", frontendCallbackUrl)
            }
        )
        return AuthUrlResponse("google", url, state)
    }

    fun handleGoogleCallback(code: String, state: String): AuthLoginResponse {
        if (!googleOAuthService.verifyState(state)) {
            throw InvalidRequestException(ErrorCode.INVALID_STATE)
        }
        val tokenRes = googleOAuthService.exchangeCodeForTokens(code)
        val userInfoRes = googleOAuthService.fetchUserInfo(tokenRes.accessToken)
        val googleUser = GoogleUserInfo(
            sub = userInfoRes.sub,
            email = userInfoRes.email,
            emailVerified = userInfoRes.emailVerified,
            name = userInfoRes.name,
            givenName = userInfoRes.givenName,
            familyName = userInfoRes.familyName,
            picture = userInfoRes.picture,
        )
        val savedUser = userService.getOrCreateUserFromGoogle(
            googleSub = googleUser.sub,
            email = googleUser.email,
            name = googleUser.name,
            pictureUrl = googleUser.picture,
        )
        val sessionToken = sessionAuthService.createSession(
            UserPrincipal(
                subject = savedUser.id.toString(),
                email = googleUser.email,
                name = googleUser.name,
                picture = googleUser.picture,
                provider = "google",
                roles = setOf(SecurityAuthorities.ROLE_USER)
            )
        )
        logger.debug { "[AuthService] Google login session created token=${sessionToken} subject=${savedUser.id}" }
        val tokens = GoogleTokenResponse(
            accessToken = tokenRes.accessToken,
            idToken = tokenRes.idToken,
            refreshToken = tokenRes.refreshToken,
            expiresInSeconds = tokenRes.expiresInSeconds,
            scope = tokenRes.scope,
            tokenType = tokenRes.tokenType
        )
        return AuthLoginResponse(
            provider = "google",
            google = googleUser,
            googleTokens = tokens,
            appSessionToken = sessionToken,
            jwt = null
        )
    }

    fun buildFrontendCallbackRedirect(code: String, state: String): String {
        val frontendCallback = googleOAuthService.extractFieldFromState(state, "frontendCallbackUrl")
            ?: "https://www.honmoon.site/auth/google/callback"
        val redirectAfter = googleOAuthService.extractFieldFromState(state, "redirectAfter")
        val sep = if (frontendCallback.contains('?')) '&' else '?'
        val encodedCode = java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8)
        val encodedState = java.net.URLEncoder.encode(state, java.nio.charset.StandardCharsets.UTF_8)
        val base = "$frontendCallback${sep}code=$encodedCode&state=$encodedState"
        return if (redirectAfter.isNullOrBlank()) base else {
            val encodedAfter = java.net.URLEncoder.encode(redirectAfter, java.nio.charset.StandardCharsets.UTF_8)
            "$base&redirectAfter=$encodedAfter"
        }
    }

    fun sendSignupMagicLink(body: EmailSignUpRequest, frontendRedirectUrl: String? = null): EmailMagicLinkResponse {
        val token = magicLinkService.issue(body.email, 15)
        val magicLink = "https://www.honmoon-api.site/api/auth/email/callback?token=$token&purpose=signup" +
            if (frontendRedirectUrl != null) "&redirectUrl=$frontendRedirectUrl" else ""
        val expiresAt = LocalDateTime.now().plusMinutes(60)
        emailService.sendMagicLinkHtml(body.email, magicLink, purpose = "회원가입", name = body.name)
        return EmailMagicLinkResponse(body.email, magicLink, expiresAt)
    }


    fun sendLoginMagicLinkByUserId(request: EmailLoginByUserRequest, frontendRedirectUrl: String? = null): EmailMagicLinkResponse {
        val email = userService.getEmailByUserId(request.userId)
        val token = magicLinkService.issue(email, 15)
        val magicLink = "https://www.honmoon-api.site/api/auth/email/callback?token=$token&purpose=login" +
            if (frontendRedirectUrl != null) "&redirectUrl=$frontendRedirectUrl" else ""
        val expiresAt = LocalDateTime.now().plusMinutes(60)
        emailService.sendMagicLinkHtml(email, magicLink, purpose = "로그인")
        return EmailMagicLinkResponse(email, magicLink, expiresAt)
    }

    fun handleMagicLinkCallback(token: String, purpose: String?, redirectUrl: String?): ResponseEntity<Void> {
        val email = magicLinkService.verify(token)
        if (email == null) {
            throw AuthException(ErrorCode.INVALID_OR_EXPIRED_TOKEN)
        }
        val savedUser = userService.getOrCreateUserByEmail(email)
        val sessionToken = sessionAuthService.createSession(
            UserPrincipal(
                subject = savedUser.id.toString(),
                email = savedUser.email,
                name = savedUser.nickname ?: savedUser.email ?: savedUser.id.toString(),
                picture = savedUser.profileImageUrl,
                provider = "email",
                roles = setOf(SecurityAuthorities.ROLE_USER)
            )
        )
        logger.debug { "[AuthService] Magic-link session created token=${sessionToken} subject=${savedUser.id}" }
        val finalRedirectUrl = redirectUrl ?: "https://www.honmoon.site"
        val redirectUrlWithToken = "$finalRedirectUrl#token=$sessionToken&email=$email&purpose=${purpose ?: "login"}"
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, redirectUrlWithToken).build()
    }

    /**
     * 이메일 매직 토큰을 검증하고 세션 토큰을 JSON으로 반환한다.
     */
    fun exchangeEmailMagicToken(token: String, purpose: String?): EmailCallbackResponse {
        val email = magicLinkService.verify(token)
        if (email == null) {
            throw AuthException(ErrorCode.INVALID_OR_EXPIRED_TOKEN)
        }
        val savedUser = userService.getOrCreateUserByEmail(email)
        val sessionToken = sessionAuthService.createSession(
            UserPrincipal(
                subject = savedUser.id.toString(),
                email = savedUser.email,
                name = savedUser.nickname ?: savedUser.email ?: savedUser.id.toString(),
                picture = savedUser.profileImageUrl,
                provider = "email",
                roles = setOf(SecurityAuthorities.ROLE_USER)
            )
        )
        logger.debug { "[AuthService] Magic-link session created (JSON) token=${sessionToken} subject=${savedUser.id}" }
        return EmailCallbackResponse(
            email = email,
            isValid = true,
            appSessionToken = sessionToken
        )
    }

    fun buildCurrentProfile(principal: UserPrincipal?): ProfileResponse {
        if (principal == null) {
            throw AuthException(ErrorCode.UNAUTHORIZED)
        }
        return ProfileResponse(
            sub = principal.subject,
            email = principal.email,
            name = principal.name,
            picture = principal.picture,
            provider = principal.provider
        )
    }

    fun logout(authorization: String?): LogoutResponse {
        val token = authorization?.removePrefix("Bearer ")?.trim()
        if (!token.isNullOrBlank()) {
            sessionAuthService.invalidate(token)
        }
        return LogoutResponse(true)
    }

    /**
     * 이메일/비밀번호 로그인 처리. 세션 토큰을 발급한다.
     */
    fun loginWithEmailPassword(email: String, rawPassword: String): AuthLoginResponse {
        val user = userService.getByEmailOrThrow(email)
        val hash = user.passwordHash
        if (hash.isNullOrBlank() || !passwordEncoder.matches(rawPassword, hash)) {
            throw AuthException(ErrorCode.UNAUTHORIZED)
        }
        val sessionToken = sessionAuthService.createSession(
            UserPrincipal(
                subject = user.id.toString(),
                email = user.email,
                name = user.nickname ?: user.email ?: user.id.toString(),
                picture = user.profileImageUrl,
                provider = "email",
                roles = setOf(SecurityAuthorities.ROLE_USER)
            )
        )
        return AuthLoginResponse(
            provider = "email",
            google = null,
            googleTokens = null,
            appSessionToken = sessionToken
        )
    }

    /**
     * 현재 사용자 비밀번호를 설정/변경한다.
     */
    @Transactional
    fun setPassword(userId: java.util.UUID, rawPassword: String) {
        if (rawPassword.length < 8) {
            throw InvalidRequestException(ErrorCode.ILLEGAL_REQUEST, "비밀번호는 8자 이상이어야 합니다.")
        }
        val encoded = passwordEncoder.encode(rawPassword)
        userService.updatePasswordHash(userId, encoded)
    }

    /**
     * Basic 인증된 호출자에게 실제 OAuth 사용자와 동일한 세션 토큰을 발급한다.
     * 이메일 오름차순 첫 번째 사용자를 선택한다.
     */
    @Transactional
    fun issueTestTokenForBasicAuth(): BasicTokenResponse {
        val user = userService.getFirstUserByEmailAscOrThrow()

        val oauthLikePrincipal = UserPrincipal(
            subject = user.id.toString(),
            email = user.email,
            name = user.nickname ?: user.email ?: user.id.toString(),
            picture = user.profileImageUrl,
            provider = "google",
            roles = setOf(SecurityAuthorities.ROLE_USER),
        )

        val token = sessionAuthService.createSession(oauthLikePrincipal)
        logger.info { "[AuthService] Test session created token=${token} subject=${user.id} email=${user.email}" }
        val expiresAt = Instant.now().plusSeconds(60L * 60L * 24L * 7L)
        return BasicTokenResponse(token = token, expiresAt = expiresAt)
    }
}
