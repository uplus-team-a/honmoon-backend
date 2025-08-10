package site.honmoon.user.dto

import java.time.Instant
import java.util.*

data class UserSummaryResponse(
    val id: Long,
    val userId: UUID,
    val totalPoints: Int,
    val totalActivities: Int,
    val createdAt: Instant,
    val modifiedAt: Instant
) 