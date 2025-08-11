package site.honmoon.auth.service

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import site.honmoon.auth.dto.*
import site.honmoon.auth.security.SessionAuthService
import site.honmoon.auth.security.UserPrincipal
import java.time.LocalDateTime
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.AuthException
import site.honmoon.common.exception.InvalidRequestException
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val googleOAuthService: GoogleOAuthService,
    private val sessionAuthService: SessionAuthService,
    private val emailService: EmailService,
    private val magicLinkService: MagicLinkService,
    private val userService: site.honmoon.user.service.UserService,
) {
    fun buildGoogleAuthUrl(scope: String, redirectAfter: String?): AuthUrlResponse {
        val scopes = scope.split(' ').filter { it.isNotBlank() }
        val (url, state, _) = googleOAuthService.buildAuthorizationUrl(
            scopes = scopes,
            statePayload = if (redirectAfter != null) mapOf("redirectAfter" to redirectAfter) else emptyMap()
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
                roles = setOf("ROLE_USER")
            )
        )
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

    fun sendSignupMagicLink(body: EmailSignUpRequest): EmailMagicLinkResponse {
        val token = magicLinkService.issue(body.email, 15)
        val magicLink = "https://honmoon.site/api/auth/email/callback?token=$token&purpose=signup"
        val expiresAt = LocalDateTime.now().plusMinutes(15)
        emailService.sendMagicLinkHtml(body.email, magicLink, purpose = "회원가입", name=body.name)
        return EmailMagicLinkResponse(body.email, magicLink, expiresAt)
    }


    fun sendLoginMagicLinkByUserId(request: EmailLoginByUserRequest): EmailMagicLinkResponse {
        val email = userService.getEmailByUserId(request.userId)
        val token = magicLinkService.issue(email, 15)
        val magicLink = "https://honmoon.site/api/auth/email/callback?token=$token&purpose=login"
        val expiresAt = LocalDateTime.now().plusMinutes(15)
        emailService.sendMagicLinkHtml(email, magicLink, purpose = "로그인")
        return EmailMagicLinkResponse(email, magicLink, expiresAt)
    }

    fun handleMagicLinkCallback(token: String, purpose: String?): ResponseEntity<Void> {
        val email = magicLinkService.verify(token)
        if (email == null) {
            throw AuthException(ErrorCode.INVALID_OR_EXPIRED_TOKEN)
        }
        val sessionToken = sessionAuthService.createSession(
            UserPrincipal(
                subject = email,
                email = email,
                name = email.substringBefore('@'),
                picture = null,
                provider = "email",
                roles = setOf("ROLE_USER")
            )
        )
        val redirectUrl =
            "https://honmoon.site/api/auth/email/callback#token=$sessionToken&email=$email&purpose=${purpose ?: "login"}"
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, redirectUrl).build()
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
     * Basic 인증된 호출자에게 실제 OAuth 사용자와 동일한 세션 토큰을 발급한다.
     * 고정 사용자: cf9f9ba7-d1fb-4240-9521-1b1e9a8d8808 (sylvester7412@gmail.com)
     */
    fun issueTestTokenForBasicAuth(@Suppress("UNUSED_PARAMETER") principal: UserPrincipal): BasicTokenResponse {
        val fixedUserId = UUID.fromString("cf9f9ba7-d1fb-4240-9521-1b1e9a8d8808")
        val user = userService.getByIdOrThrow(fixedUserId)

        val oauthLikePrincipal = UserPrincipal(
            subject = user.id.toString(),
            email = user.email,
            name = user.nickname ?: user.email ?: user.id.toString(),
            picture = user.profileImageUrl,
            provider = "google",
            roles = setOf("ROLE_USER"),
        )

        val token = sessionAuthService.createSession(oauthLikePrincipal)
        val expiresAt = Instant.now().plusSeconds(60L * 60L * 24L * 7L)
        return BasicTokenResponse(token = token, expiresAt = expiresAt)
    }
}
