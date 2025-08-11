package site.honmoon.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.auth.entity.AuthSession
import java.time.Instant

@Repository
interface AuthSessionRepository : JpaRepository<AuthSession, String> {
    fun deleteByExpiresAtBefore(now: Instant): Long
}


