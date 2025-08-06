package site.honmoon.common.client.google

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import site.honmoon.auth.dto.GoogleTokenApiResponse
import site.honmoon.auth.dto.GoogleUserInfoApiResponse

@FeignClient(name = "google-oauth", url = "https://oauth2.googleapis.com")
interface GoogleOAuthApi {
    @PostMapping(value = ["/token"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun token(
        @RequestParam("code") code: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("grant_type") grantType: String = "authorization_code",
    ): GoogleTokenApiResponse
}

@FeignClient(name = "google-userinfo", url = "https://www.googleapis.com")
interface GoogleUserInfoApi {
    @GetMapping("/oauth2/v3/userinfo")
    fun userInfo(@RequestHeader("Authorization") authorization: String): GoogleUserInfoApiResponse
} 
