package site.honmoon.auth.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.auth.entity.AuthSession
import site.honmoon.auth.repository.AuthSessionRepository
import java.time.Instant
import java.util.*

/**
 * 앱 세션을 DB(`app_session`)에 저장/검증/무효화한다.
 * OAuth/이메일 인증 이후 발급된 토큰은 Bearer 헤더로 전달되어 인증에 사용된다.
 */
@Service
class SessionAuthService(
    private val authSessionRepository: AuthSessionRepository,
) {
    private val logger = KotlinLogging.logger {}
    private val defaultTtlSeconds: Long = 60L * 60L * 24L * 7L

    /**
     * 주어진 사용자 정보로 세션을 생성하고 토큰을 반환한다.
     */
    @Transactional
    fun createSession(principal: UserPrincipal, ttlSeconds: Long = defaultTtlSeconds): String {
        val token = UUID.randomUUID().toString().replace("-", "")
        val session = AuthSession(
            token = token,
            subject = principal.subject,
            email = principal.email,
            name = principal.name,
            picture = principal.picture,
            provider = principal.provider,
            expiresAt = Instant.now().plusSeconds(ttlSeconds)
        )
        authSessionRepository.save(session)
        return token
    }

    /**
     * 토큰으로 세션을 조회/검증하고 유효하면 `UserPrincipal`을 반환한다. 만료 또는 미존재 시 null.
     */
    @Transactional(readOnly = true)
    fun authenticate(token: String): UserPrincipal? {
        val session = authSessionRepository.findById(token).orElse(null) ?: return null
        if (session.expiresAt.isBefore(Instant.now())) {
            return null
        }
        return UserPrincipal(
            subject = session.subject,
            email = session.email,
            name = session.name,
            picture = session.picture,
            provider = session.provider,
            roles = setOf("ROLE_USER")
        )
    }

    /**
     * 토큰 기반 세션을 무효화한다.
     */
    @Transactional
    fun invalidate(token: String) {
        authSessionRepository.deleteById(token)
    }
}
