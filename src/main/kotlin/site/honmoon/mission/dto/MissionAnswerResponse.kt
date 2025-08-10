package site.honmoon.mission.dto

data class MissionAnswerResponse(
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val explanation: String?,
    val hint: String? = null
)