package site.honmoon.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "이메일 인증 요청")
data class EmailAuthRequest(
    @Schema(description = "이메일 주소", example = "user@example.com")
    val email: String
)

@Schema(description = "로그인 요청")
data class LoginRequest(
    @Schema(description = "이메일 주소", example = "user@example.com")
    val email: String,
    
    @Schema(description = "비밀번호", example = "password123")
    val password: String
)

@Schema(description = "로그인 응답")
data class LoginResponse(
    @Schema(description = "성공 여부")
    val success: Boolean,
    
    @Schema(description = "사용자 ID")
    val userId: UUID? = null,
    
    @Schema(description = "JWT 토큰")
    val token: String? = null,
    
    @Schema(description = "메시지")
    val message: String,
    
    @Schema(description = "이메일")
    val email: String? = null,
    
    @Schema(description = "닉네임")
    val nickname: String? = null
)

@Schema(description = "이메일 인증 응답")
data class EmailAuthResponse(
    @Schema(description = "성공 여부")
    val success: Boolean,
    
    @Schema(description = "메시지")
    val message: String
)

@Schema(description = "토큰 검증 요청")
data class TokenVerifyRequest(
    @Schema(description = "인증 토큰", example = "abc123...")
    val token: String,
    
    @Schema(description = "닉네임", example = "홍길동")
    val nickname: String,
    
    @Schema(description = "비밀번호", example = "password123")
    val password: String
)

@Schema(description = "토큰 검증 응답")
data class TokenVerifyResponse(
    @Schema(description = "성공 여부")
    val success: Boolean,
    
    @Schema(description = "사용자 ID (회원가입 완료시)")
    val userId: UUID? = null,
    
    @Schema(description = "JWT 토큰")
    val token: String? = null,
    
    @Schema(description = "메시지")
    val message: String,
    
    @Schema(description = "이메일")
    val email: String? = null,
    
    @Schema(description = "닉네임")
    val nickname: String? = null
)
