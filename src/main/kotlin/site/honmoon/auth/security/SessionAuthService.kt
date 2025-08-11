package site.honmoon.auth.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class SessionAuthService {
    private val logger = KotlinLogging.logger {}

    private val tokenToSession: MutableMap<String, SessionEntry> = ConcurrentHashMap()
    private val defaultTtlSeconds: Long = 60L * 60L * 24L * 7L

    data class SessionEntry(
        val principal: UserPrincipal,
        val expiresAt: Instant,
    )

    fun createSession(principal: UserPrincipal, ttlSeconds: Long = defaultTtlSeconds): String {
        val token = UUID.randomUUID().toString().replace("-", "")
        tokenToSession[token] = SessionEntry(principal, Instant.now().plusSeconds(ttlSeconds))
        return token
    }

    fun authenticate(token: String): UserPrincipal? {
        val entry = tokenToSession[token] ?: return null
        if (entry.expiresAt.isBefore(Instant.now())) {
            tokenToSession.remove(token)
            return null
        }
        return entry.principal
    }

    fun invalidate(token: String) {
        tokenToSession.remove(token)
    }
} 
