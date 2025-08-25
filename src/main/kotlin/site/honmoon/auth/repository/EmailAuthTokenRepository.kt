package site.honmoon.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import site.honmoon.auth.entity.EmailAuthToken
import java.time.LocalDateTime

@Repository
interface EmailAuthTokenRepository : JpaRepository<EmailAuthToken, Long> {
    
    fun findByTokenAndUsedAndExpiresAtAfter(token: String, used: Boolean, now: LocalDateTime): EmailAuthToken?
    
    @Modifying
    @Query("UPDATE EmailAuthToken e SET e.used = true WHERE e.email = :email AND e.used = false")
    fun markAllUsedByEmail(email: String)
}
