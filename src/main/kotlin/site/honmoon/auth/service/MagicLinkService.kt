package site.honmoon.auth.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class MagicLinkService {
    private val logger = KotlinLogging.logger {}

    data class MagicEntry(
        val email: String,
        val expiresAt: Instant,
    )

    private val tokenToEntry: MutableMap<String, MagicEntry> = ConcurrentHashMap()

    fun issue(email: String, ttlMinutes: Long = 15): String {
        val token = java.util.UUID.randomUUID().toString().replace("-", "")
        tokenToEntry[token] = MagicEntry(email, Instant.now().plusSeconds(ttlMinutes * 60))
        return token
    }

    fun verify(token: String): String? {
        val entry = tokenToEntry[token] ?: return null
        if (entry.expiresAt.isBefore(Instant.now())) {
            tokenToEntry.remove(token)
            return null
        }
        tokenToEntry.remove(token)
        return entry.email
    }
}


