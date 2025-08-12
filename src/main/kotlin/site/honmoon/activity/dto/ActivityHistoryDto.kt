package site.honmoon.activity.dto

import site.honmoon.mission.dto.AnswerCheckResult
import java.time.Instant
import java.util.*

data class UserActivityResponse(
    val id: Long,
    val userId: UUID,
    val placeId: Long,
    val missionId: Long?,
    val description: String?,
    val isCorrect: Boolean?,
    val isCompleted: Boolean,
    val pointsEarned: Int,
    val textAnswer: String?,
    val selectedChoiceIndex: Int?,
    val uploadedImageUrl: String?,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val aiResult: AnswerCheckResult? = null,
    val alreadyExists: Boolean = false,
)
