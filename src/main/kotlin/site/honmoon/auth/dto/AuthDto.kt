package site.honmoon.auth.dto

import java.time.Instant
import java.time.LocalDateTime
import java.util.*


data class AuthUrlResponse(
    val provider: String,
    val authorizationUrl: String,
    val state: String,
)

data class GoogleTokenResponse(
    val accessToken: String,
    val idToken: String?,
    val refreshToken: String?,
    val expiresInSeconds: Long,
    val scope: String?,
    val tokenType: String,
)

data class GoogleUserInfo(
    val sub: String,
    val email: String?,
    val emailVerified: Boolean?,
    val name: String?,
    val givenName: String?,
    val familyName: String?,
    val picture: String?,
)

data class JwtTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshTokenExpiresAt: Instant,
    val tokenType: String = "Bearer",
)

data class AuthLoginResponse(
    val provider: String,
    val google: GoogleUserInfo?,
    val googleTokens: GoogleTokenResponse?,
    val appSessionToken: String?,
    val jwt: JwtTokenResponse? = null,
)

data class EmailMagicLinkResponse(
    val email: String,
    val magicLink: String,
    val expiresAt: LocalDateTime,
)

data class EmailCallbackResponse(
    val email: String,
    val isValid: Boolean,
    val appSessionToken: String?,
)

data class EmailSignUpRequest(
    val email: String,
    val name: String,
)

data class EmailLoginByUserRequest(
    val userId: UUID,
)

data class EmailPasswordLoginRequest(
    val email: String,
    val password: String,
)

data class SetPasswordRequest(
    val password: String,
)

data class LogoutResponse(
    val success: Boolean,
)

data class ProfileResponse(
    val sub: String,
    val email: String?,
    val name: String?,
    val picture: String?,
    val provider: String,
)

/**
 * Basic 인증 성공 시 발급되는 테스트용 세션 토큰 응답
 */
data class BasicTokenResponse(
    val token: String,
    val expiresAt: Instant,
)

data class GoogleCodeExchangeRequest(
    val code: String,
    val state: String,
)

data class EmailMagicTokenExchangeRequest(
    val token: String,
    val purpose: String? = null,
)
