package site.honmoon.user.dto

import java.time.Instant
import java.util.*

data class UserResponse(
    val id: UUID,
    val email: String?,
    val nickname: String?,
    val totalPoints: Int,
    val totalActivities: Int,
    val profileImageUrl: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val modifiedAt: Instant,
)

data class UpdateUserRequest(
    val nickname: String?,
    val profileImageUrl: String?,
)