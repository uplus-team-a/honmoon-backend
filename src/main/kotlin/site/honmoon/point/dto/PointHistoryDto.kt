package site.honmoon.point.dto

import java.time.Instant
import java.util.*

data class PointHistoryResponse(
    val id: Long,
    val userId: UUID,
    val points: Int,
    val description: String,
    val createdAt: Instant,
    val modifiedAt: Instant,
) 

data class UsePointsResult(
    val success: Boolean,
    val requiredPoints: Int,
    val currentPoints: Int,
    val remainingPoints: Int?,
    val history: PointHistoryResponse?,
    val reasonCode: String? = null,
)
