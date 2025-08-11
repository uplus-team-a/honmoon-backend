package site.honmoon.auth.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import site.honmoon.auth.repository.AuthSessionRepository
import site.honmoon.auth.repository.MagicLinkTokenRepository
import java.time.Instant

@Component
class AuthCleanupScheduler(
    private val authSessionRepository: AuthSessionRepository,
    private val magicLinkTokenRepository: MagicLinkTokenRepository,
) {
    private val logger = KotlinLogging.logger {}

    // 매 10분마다 만료 세션/매직링크를 정리. 여러 인스턴스에서 동시에 실행되어도 무관(조건부 delete).
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    fun cleanExpired() {
        val now = Instant.now()
        val removedSessions = authSessionRepository.deleteByExpiresAtBefore(now)
        val removedMagicLinks = magicLinkTokenRepository.deleteByExpiresAtBefore(now)
        if (removedSessions + removedMagicLinks > 0) {
            logger.info { "auth cleanup removed sessions=$removedSessions, magicLinks=$removedMagicLinks" }
        }
    }
}


