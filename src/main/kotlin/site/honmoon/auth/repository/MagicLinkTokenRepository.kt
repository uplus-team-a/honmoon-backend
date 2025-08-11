package site.honmoon.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.auth.entity.MagicLinkToken
import java.time.Instant

@Repository
interface MagicLinkTokenRepository : JpaRepository<MagicLinkToken, String> {
    fun deleteByExpiresAtBefore(now: Instant): Long
}


