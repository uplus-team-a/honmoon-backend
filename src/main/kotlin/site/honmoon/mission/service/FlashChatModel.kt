package site.honmoon.mission.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.web.reactive.function.client.WebClient
import site.honmoon.mission.config.GeminiChatProperties

@Component
class FlashChatModel(
    webClientBuilder: WebClient.Builder,
    private val chatProperties: GeminiChatProperties,
) : ChatModel {

    private val webClient: WebClient = webClientBuilder
        .baseUrl(chatProperties.chat.baseUrl)
        .build()

    override fun call(prompt: Prompt): ChatResponse {
        val combinedText = prompt.instructions
            .joinToString(separator = "\n") { it.text.replace("\n", " ") }

        val requestBody: Map<String, List<Map<String, Any?>>> = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to combinedText)
                    )
                )
            )
        )

        val resp = webClient.post()
            .uri { b ->
                b.path(chatProperties.chat.completionsPath)
                    .queryParam("key", chatProperties.apiKey)
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(FlashResponse::class.java)
            .block()

        Assert.state(resp != null && resp.candidates.isNotEmpty(), "Empty response from Flash API")

        val candidate = resp?.candidates?.firstOrNull()
            ?: throw IllegalStateException("No candidates in Flash API response")
        val text = candidate.content.parts.firstOrNull()?.text
            ?: throw IllegalStateException("No content parts in Flash API response")

        val generation = Generation(AssistantMessage(text))

        return ChatResponse(listOf(generation))
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FlashResponse(
        val candidates: List<Candidate>,
        val usageMetadata: UsageMetadata?,
        val modelVersion: String,
        val responseId: String,
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Candidate(
            val content: Content,
            val finishReason: String,
            val citationMetadata: CitationMetadata?,
            val avgLogprobs: Double,
        )

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Content(
            val parts: List<Part>,
            val role: String,
        )

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Part(val text: String)

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class CitationMetadata(
            val citationSources: List<CitationSource>,
        ) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            data class CitationSource(
                val startIndex: Int,
                val endIndex: Int,
            )
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class UsageMetadata(
            val promptTokenCount: Int,
            val candidatesTokenCount: Int,
            val totalTokenCount: Int,
            val promptTokensDetails: List<ModalityCount>,
            val candidatesTokensDetails: List<ModalityCount>,
        ) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            data class ModalityCount(
                val modality: String,
                val tokenCount: Int,
            )
        }
    }
}
