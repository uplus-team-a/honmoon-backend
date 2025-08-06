package site.honmoon.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenApiResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("id_token") val idToken: String?,
    @JsonProperty("refresh_token") val refreshToken: String?,
    @JsonProperty("expires_in") val expiresInSeconds: Long,
    val scope: String?,
    @JsonProperty("token_type") val tokenType: String,
)

data class GoogleUserInfoApiResponse(
    val sub: String,
    val email: String?,
    @JsonProperty("email_verified") val emailVerified: Boolean?,
    val name: String?,
    @JsonProperty("given_name") val givenName: String?,
    @JsonProperty("family_name") val familyName: String?,
    val picture: String?,
) 