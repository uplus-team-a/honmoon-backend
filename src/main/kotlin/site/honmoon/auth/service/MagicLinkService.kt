package site.honmoon.auth.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.auth.entity.MagicLinkToken
import site.honmoon.auth.repository.MagicLinkTokenRepository
import java.time.Instant
import java.util.*

@Service
class MagicLinkService(
    private val magicLinkTokenRepository: MagicLinkTokenRepository,
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun issue(email: String, ttlMinutes: Long = 15): String {
        val token = UUID.randomUUID().toString().replace("-", "")
        val entity = MagicLinkToken(
            token = token,
            email = email,
            expiresAt = Instant.now().plusSeconds(ttlMinutes * 60)
        )
        magicLinkTokenRepository.save(entity)
        return token
    }

    @Transactional
    fun verify(token: String): String? {
        val entity = magicLinkTokenRepository.findById(token).orElse(null) ?: return null
        if (entity.expiresAt.isBefore(Instant.now())) {
            magicLinkTokenRepository.deleteById(token)
            return null
        }
        magicLinkTokenRepository.deleteById(token)
        return entity.email
    }
}


