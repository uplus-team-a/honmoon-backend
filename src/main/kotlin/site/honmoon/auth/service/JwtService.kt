package site.honmoon.auth.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret:honmoon-secret-key-for-jwt-token-generation-very-long-secret}") 
    private val secretKey: String,
    
    @Value("\${app.jwt.expiration:86400000}") // 24시간 (밀리초)
    private val jwtExpiration: Long,
) {
    
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }
    
    fun generateToken(userId: String, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpiration)
        
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }
    
    fun extractUserId(token: String): String {
        return extractClaims(token).subject
    }
    
    fun extractEmail(token: String): String? {
        return extractClaims(token)["email"] as? String
    }
    
    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = extractClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }
    
    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
