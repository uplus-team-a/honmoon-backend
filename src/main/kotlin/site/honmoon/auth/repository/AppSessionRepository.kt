package site.honmoon.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.auth.entity.AppSession
import java.time.Instant

@Repository
interface AppSessionRepository : JpaRepository<AppSession, String> {
    fun deleteByExpiresAtBefore(now: Instant): Long
    fun findFirstBySubjectOrderByCreatedAtDesc(subject: String): AppSession?
}


