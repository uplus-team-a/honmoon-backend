package site.honmoon.mission.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.model.Media
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import site.honmoon.mission.dto.AnswerCheckResult
import site.honmoon.mission.dto.ImageAnalysisResult
import site.honmoon.mission.entity.MissionDetail

@Service
@Qualifier("openai")
class OpenAIService(
    @Qualifier("openaiChatClient") private val chatClient: ChatClient,
    private val promptService: OpenAIPromptService,
    private val imageUrlValidator: ImageUrlValidator
) : AIService {

    override fun analyzeImage(imageUrl: String): ImageAnalysisResult {
        return try {
            val validatedUri = imageUrlValidator.validateImageUrl(imageUrl)
            val imageResource = UrlResource(validatedUri)
            val mimeType = detectMimeTypeFromUrl(imageUrl)
            val media = Media(mimeType, imageResource)

            val response = chatClient.prompt()
                .user { userSpec ->
                    userSpec
                        .text(promptService.createImageAnalysisPrompt())
                        .media(media)
                }
                .call()
                .entity(ImageAnalysisResult::class.java)

            response ?: ImageAnalysisResult(
                extractedText = "",
                confidence = 0.0,
                description = "OpenAI 이미지 분석 실패"
            )
        } catch (e: Exception) {
            throw RuntimeException("OpenAI service error: ${e.message}", e)
        }
    }

    override fun checkTextAnswer(mission: MissionDetail, userAnswer: String): AnswerCheckResult {
        return try {
            val response = chatClient.prompt()
                .user(promptService.createAnswerCheckPrompt(mission, userAnswer))
                .call()
                .entity(AnswerCheckResult::class.java)

            response ?: AnswerCheckResult(
                isCorrect = false,
                confidence = 0.0,
                reasoning = "OpenAI 응답 파싱 실패",
                hint = ""
            )
        } catch (e: Exception) {
            throw RuntimeException("OpenAI service error: ${e.message}", e)
        }
    }

    override fun checkImageAnswer(mission: MissionDetail, extractedText: String): AnswerCheckResult {
        return try {
            val response = chatClient.prompt()
                .user(promptService.createImageAnswerCheckPrompt(mission, extractedText))
                .call()
                .entity(AnswerCheckResult::class.java)

            response ?: AnswerCheckResult(
                isCorrect = false,
                confidence = 0.0,
                reasoning = "OpenAI 응답 파싱 실패",
                hint = ""
            )
        } catch (e: Exception) {
            throw RuntimeException("OpenAI service error: ${e.message}", e)
        }
    }

    override fun isAvailable(): Boolean {
        return try {
            val testResponse = chatClient.prompt()
                .user("테스트")
                .call()
                .content()
            testResponse != null
        } catch (e: Exception) {
            false
        }
    }

    override fun getProviderName(): String = "OpenAI"

    private fun detectMimeTypeFromUrl(url: String): MimeType {
        val lower = url.lowercase()
        return when {
            lower.endsWith(".png") -> MimeType.valueOf("image/png")
            lower.endsWith(".webp") -> MimeType.valueOf("image/webp")
            lower.endsWith(".gif") -> MimeType.valueOf("image/gif")
            lower.endsWith(".bmp") -> MimeType.valueOf("image/bmp")
            else -> MimeType.valueOf("image/jpeg")
        }
    }
}