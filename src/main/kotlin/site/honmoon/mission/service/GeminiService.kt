package site.honmoon.mission.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import site.honmoon.mission.dto.AnswerCheckResult
import site.honmoon.mission.dto.ImageAnalysisResult
import site.honmoon.mission.entity.MissionDetail

@Service
@Qualifier("gemini")
class GeminiService(
    private val flashChatModel: FlashChatModel,
    private val promptService: OpenAIPromptService,
    private val imageUrlValidator: ImageUrlValidator,
) : AIService {

    private val chatClient = ChatClient.builder(flashChatModel).build()

    override fun analyzeImage(imageUrl: String): ImageAnalysisResult {
        return try {
            imageUrlValidator.validateImageUrl(imageUrl)

            ImageAnalysisResult(
                extractedText = "Gemini Flash does not support image analysis in this implementation",
                confidence = 0.0,
                description = "Image analysis not implemented for Gemini Flash"
            )
        } catch (e: Exception) {
            throw RuntimeException("Gemini service error: ${e.message}", e)
        }
    }

    override fun checkTextAnswer(mission: MissionDetail, userAnswer: String): AnswerCheckResult {
        return try {
            val prompt = promptService.createAnswerCheckPrompt(mission, userAnswer)

            val response = chatClient.prompt(prompt).call().content()

            parseJsonResponse(response ?: "")
        } catch (e: Exception) {
            throw RuntimeException("Gemini service error: ${e.message}", e)
        }
    }

    override fun checkImageAnswer(mission: MissionDetail, extractedText: String): AnswerCheckResult {
        return try {
            val prompt = promptService.createImageAnswerCheckPrompt(mission, extractedText)

            val response = chatClient.prompt(prompt).call().content()

            parseJsonResponse(response ?: "")
        } catch (e: Exception) {
            throw RuntimeException("Gemini service error: ${e.message}", e)
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            val response = chatClient.prompt("test").call().content()
            response?.isNotBlank() == true
        } catch (e: Exception) {
            false
        }
    }

    override fun getProviderName(): String = "Google Gemini Flash"

    private fun parseJsonResponse(responseText: String): AnswerCheckResult {
        val jsonStart = responseText.indexOf("{")
        val jsonEnd = responseText.lastIndexOf("}") + 1

        if (jsonStart == -1 || jsonEnd <= jsonStart) {
            throw IllegalArgumentException("No valid JSON found in response")
        }

        val jsonPart = responseText.substring(jsonStart, jsonEnd)

        val isCorrect = jsonPart.contains("\"isCorrect\"\\s*:\\s*true".toRegex())
        val confidenceMatch = "\"confidence\"\\s*:\\s*([0-9.]+)".toRegex().find(jsonPart)
        val confidence = confidenceMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

        val reasoningMatch = "\"reasoning\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(jsonPart)
        val reasoning = reasoningMatch?.groupValues?.get(1) ?: "No reasoning provided"

        val hintMatch = "\"hint\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(jsonPart)
        val hint = hintMatch?.groupValues?.get(1)

        return AnswerCheckResult(
            isCorrect = isCorrect,
            confidence = confidence,
            reasoning = reasoning,
            hint = if (!isCorrect) hint?.takeIf { it.isNotBlank() } else ""
        )
    }
}
