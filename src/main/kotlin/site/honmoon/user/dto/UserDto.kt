package site.honmoon.user.dto

import site.honmoon.activity.dto.UserActivityResponse
import site.honmoon.point.dto.PointHistoryResponse
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

data class UpdateProfileImageRequest(
    val imageUrl: String,
)

data class UserProfileSummaryResponse(
    val profile: UserResponse,
    val pointsSummary: Map<String, Int>,
    val recentActivities: List<UserActivityResponse>,
    val recentPointHistory: List<PointHistoryResponse>,
)

data class UserProfileDetailResponse(
    val profile: UserResponse,
    val pointsSummary: Map<String, Int>,
    val activities: List<UserActivityResponse>,
    val pointHistory: List<PointHistoryResponse>,
)