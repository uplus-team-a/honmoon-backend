package site.honmoon.auth.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.auth.entity.AppSession
import site.honmoon.auth.repository.AppSessionRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * 앱 세션을 DB(`app_session`)에 저장/검증/무효화한다.
 * OAuth/이메일 인증 이후 발급된 토큰은 Bearer 헤더로 전달되어 인증에 사용된다.
 */
@Service
class SessionAuthService(
    private val appSessionRepository: AppSessionRepository,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 주어진 사용자 정보로 세션을 생성하고 토큰을 반환한다.
     */
    @Transactional
    fun createSession(principal: UserPrincipal): String {
        val now = Instant.now()
        val existing = appSessionRepository.findFirstBySubjectOrderByCreatedAtDesc(principal.subject)
        val newToken = UUID.randomUUID().toString().replace("-", "")

        if (existing != null) {
            // 기존 세션 토큰을 재사용하지 않고, 토큰을 교체하여 업데이트
            existing.token = newToken
            existing.email = principal.email
            existing.name = principal.name
            existing.picture = principal.picture
            existing.provider = principal.provider
            existing.expiresAt = now.plus(60, ChronoUnit.MINUTES)
            appSessionRepository.save(existing)
            logger.info { "[Session] updated token=${existing.token} subject=${existing.subject} provider=${existing.provider} email=${existing.email} expiresAt=${existing.expiresAt}" }
            return existing.token
        }

        val session = AppSession(
            token = newToken,
            subject = principal.subject,
            email = principal.email,
            name = principal.name,
            picture = principal.picture,
            provider = principal.provider,
            expiresAt = now.plus(60, ChronoUnit.MINUTES)
        )
        appSessionRepository.save(session)
        logger.info { "[Session] created token=${newToken} subject=${session.subject} provider=${session.provider} email=${session.email} expiresAt=${session.expiresAt}" }
        return newToken
    }

    /**
     * 토큰으로 세션을 조회/검증하고 유효하면 `UserPrincipal`을 반환한다. 만료 또는 미존재 시 null.
     */
    @Transactional(readOnly = true)
    fun authenticate(token: String): UserPrincipal? {
        val session = appSessionRepository.findById(token).orElse(null)
        if (session == null) {
            logger.warn { "[Session] not found token=${token}" }
            return null
        }
        if (session.expiresAt.isBefore(Instant.now())) {
            logger.warn { "[Session] expired token=${token} expiresAt=${session.expiresAt} now=${Instant.now()}" }
            return null
        }
        return UserPrincipal(
            subject = session.subject,
            email = session.email,
            name = session.name,
            picture = session.picture,
            provider = session.provider,
            roles = setOf(SecurityAuthorities.ROLE_USER)
        )
    }

    /**
     * 토큰 기반 세션을 무효화한다.
     */
    @Transactional
    fun invalidate(token: String) {
        appSessionRepository.deleteById(token)
    }
}
