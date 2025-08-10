package site.honmoon.mission.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import site.honmoon.mission.dto.AnswerCheckResult
import site.honmoon.mission.dto.ImageAnalysisResult
import site.honmoon.mission.entity.MissionDetail

@Service
class FallbackAIService(
    @Qualifier("openai") private val openAIService: AIService,
    @Qualifier("gemini") private val geminiService: AIService
) : AIService {

    private val logger = LoggerFactory.getLogger(FallbackAIService::class.java)

    override fun analyzeImage(imageUrl: String): ImageAnalysisResult {
        return tryWithFallback(
            primaryAction = { openAIService.analyzeImage(imageUrl) },
            fallbackAction = { geminiService.analyzeImage(imageUrl) },
            operationName = "이미지 분석"
        )
    }

    override fun checkTextAnswer(mission: MissionDetail, userAnswer: String): AnswerCheckResult {
        return tryWithFallback(
            primaryAction = { openAIService.checkTextAnswer(mission, userAnswer) },
            fallbackAction = { geminiService.checkTextAnswer(mission, userAnswer) },
            operationName = "텍스트 정답 판별"
        )
    }

    override fun checkImageAnswer(mission: MissionDetail, extractedText: String): AnswerCheckResult {
        return tryWithFallback(
            primaryAction = { openAIService.checkImageAnswer(mission, extractedText) },
            fallbackAction = { geminiService.checkImageAnswer(mission, extractedText) },
            operationName = "이미지 정답 판별"
        )
    }

    override fun isAvailable(): Boolean {
        return openAIService.isAvailable() || geminiService.isAvailable()
    }

    override fun getProviderName(): String {
        return when {
            openAIService.isAvailable() -> openAIService.getProviderName()
            geminiService.isAvailable() -> geminiService.getProviderName()
            else -> "No AI Service Available"
        }
    }

    private fun <T> tryWithFallback(
        primaryAction: () -> T,
        fallbackAction: () -> T,
        operationName: String
    ): T {
        return try {
            logger.debug("Trying primary AI service (OpenAI) for $operationName")
            primaryAction()
        } catch (e: Exception) {
            logger.warn("Primary AI service failed for $operationName: ${e.message}, trying fallback (Gemini)")
            try {
                fallbackAction()
            } catch (fallbackException: Exception) {
                logger.error("Both AI services failed for $operationName. OpenAI: ${e.message}, Gemini: ${fallbackException.message}")
                throw RuntimeException("All AI services failed for $operationName", fallbackException)
            }
        }
    }
}