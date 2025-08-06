package site.honmoon.auth.dto

import java.time.LocalDateTime
import java.time.Instant

// 요청 DTO
data class EmailLoginRequest(
    val email: String
)

data class TokenRefreshRequest(
    val refreshToken: String
)

// 응답 DTO

data class AuthUrlResponse(
    val provider: String,
    val authorizationUrl: String,
    val state: String
)

data class GoogleTokenResponse(
    val accessToken: String,
    val idToken: String?,
    val refreshToken: String?,
    val expiresInSeconds: Long,
    val scope: String?,
    val tokenType: String
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
    val jwt: JwtTokenResponse? = null
)

data class EmailMagicLinkResponse(
    val email: String,
    val magicLink: String,
    val expiresAt: LocalDateTime
)

data class EmailCallbackResponse(
    val email: String,
    val isValid: Boolean,
    val appSessionToken: String?
)

data class ProfileResponse(
    val sub: String,
    val email: String?,
    val name: String?,
    val picture: String?,
    val provider: String,
) 