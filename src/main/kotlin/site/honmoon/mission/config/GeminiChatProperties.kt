package site.honmoon.mission.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "spring.ai.gemini")
data class GeminiChatProperties(
    var apiKey: String = "",
    var chat: Chat = Chat(),
) {
    data class Chat(
        var completionsPath: String = "/v1beta/models/gemini-2.5-flash:generateContent",
        var baseUrl: String = "https://generativelanguage.googleapis.com",
        var options: Options = Options(),
    )

    data class Options(
        var model: String = "gemini-2.5-flash",
        var temperature: Double = 0.1,
    )
}
