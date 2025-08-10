package site.honmoon.mission.dto

data class ImageAnalysisResult(
    val extractedText: String,
    val confidence: Double,
    val description: String
)