package site.honmoon.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.user.entity.UserSummary
import java.util.*

@Repository
interface UserSummaryRepository : JpaRepository<UserSummary, Long> {
    fun findByUserId(userId: UUID): UserSummary?
} 