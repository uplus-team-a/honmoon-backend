package site.honmoon.mission.dto

data class AnswerCheckResult(
    val isCorrect: Boolean,
    val confidence: Double,
    val reasoning: String,
    val extractedText: String? = null,
    val hint: String? = null
)