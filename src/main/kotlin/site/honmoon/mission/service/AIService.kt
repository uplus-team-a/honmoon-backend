package site.honmoon.mission.service

import site.honmoon.mission.dto.AnswerCheckResult
import site.honmoon.mission.dto.ImageAnalysisResult
import site.honmoon.mission.entity.MissionDetail

interface AIService {
    fun analyzeImage(imageUrl: String): ImageAnalysisResult
    fun checkTextAnswer(mission: MissionDetail, userAnswer: String): AnswerCheckResult
    fun checkImageAnswer(mission: MissionDetail, extractedText: String): AnswerCheckResult
    fun isAvailable(): Boolean
    fun getProviderName(): String
}