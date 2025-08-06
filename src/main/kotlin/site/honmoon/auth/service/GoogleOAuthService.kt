package site.honmoon.auth.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import site.honmoon.auth.dto.GoogleUserInfoApiResponse
import site.honmoon.common.client.google.GoogleOAuthApi
import site.honmoon.common.client.google.GoogleUserInfoApi
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class GoogleOAuthService(
    @Value("\${GOOGLE_CLIENT_ID}") private val clientId: String,
    @Value("\${GOOGLE_CLIENT_SECRET}") private val clientSecret: String,
    @Value("\${GOOGLE_REDIRECT_URI:https://honmoon-api.site/api/auth/google/callback}") private val redirectUri: String,
    private val oauthApi: GoogleOAuthApi,
    private val userInfoApi: GoogleUserInfoApi,
) {
    private val logger = KotlinLogging.logger {}

    private val oauthAuthorizeBase = "https://accounts.google.com/o/oauth2/v2/auth"

    fun buildAuthorizationUrl(scopes: List<String>, statePayload: Map<String, String> = emptyMap()): Triple<String, String, Instant> {
        val state = signState(statePayload)
        val params = mapOf(
            "client_id" to clientId,
            "redirect_uri" to redirectUri,
            "response_type" to "code",
            "scope" to scopes.joinToString(" "),
            "access_type" to "offline",
            "include_granted_scopes" to "true",
            "state" to state
        )
        val url = oauthAuthorizeBase + "?" + params.entries.joinToString("&") { (k, v) ->
            k + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8)
        }
        return Triple(url, state, Instant.now().plus(Duration.ofMinutes(10)))
    }

    fun exchangeCodeForTokens(code: String): site.honmoon.auth.dto.GoogleTokenApiResponse {
        return oauthApi.token(
            code = code,
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = redirectUri,
            grantType = "authorization_code"
        )
    }

    fun fetchUserInfo(accessToken: String): GoogleUserInfoApiResponse {
        return userInfoApi.userInfo("Bearer $accessToken")
    }

    fun verifyState(state: String): Boolean {
        return try {
            val parts = state.split('.')
            if (parts.size != 3) return false
            val (payloadB64, tsB64, sigHex) = parts
            val payloadBytes = Base64.getUrlDecoder().decode(payloadB64)
            val ts = String(Base64.getUrlDecoder().decode(tsB64)).toLong()
            val expected = hmacSha256Hex(payloadB64 + "." + tsB64)
            val notExpired = Instant.ofEpochSecond(ts).isAfter(Instant.now().minus(Duration.ofMinutes(15)))
            expected.equals(sigHex, ignoreCase = true) && notExpired
        } catch (e: Exception) {
            logger.warn(e) { "state verify failed" }
            false
        }
    }

    private fun signState(payload: Map<String, String>): String {
        val payloadJson = payload.entries.joinToString(prefix = "{", postfix = "}") { "\"${it.key}\":\"${it.value}\"" }
        val payloadB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.toByteArray())
        val tsB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(Instant.now().epochSecond.toString().toByteArray())
        val sig = hmacSha256Hex("$payloadB64.$tsB64")
        return "$payloadB64.$tsB64.$sig"
    }

    private fun hmacSha256Hex(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(clientSecret.toByteArray(), "HmacSHA256"))
        val bytes = mac.doFinal(data.toByteArray())
        return bytes.joinToString("") { String.format("%02x", it) }
    }
} 
