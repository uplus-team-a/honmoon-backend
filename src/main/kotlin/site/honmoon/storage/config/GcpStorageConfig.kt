package site.honmoon.storage.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream

@Configuration
class GcpStorageConfig {
    private val logger = KotlinLogging.logger { }

    @Value("\${GCP_SERVICE_ACCOUNT_JSON:}")
    private lateinit var serviceAccountJson: String

    @Bean
    fun storage(): Storage {
        val credentials = resolveCredentials()
        return if (credentials != null) {
            StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .service
        } else {
            StorageOptions.getDefaultInstance().service
        }
    }

    private fun resolveCredentials(): GoogleCredentials? {
        if (this::serviceAccountJson.isInitialized && serviceAccountJson.isNotBlank()) {
            try {
                val cleanedJson = cleanJsonString(serviceAccountJson.trim())
                return GoogleCredentials.fromStream(ByteArrayInputStream(cleanedJson.toByteArray()))
            } catch (e: Exception) {
                logger.warn { "GoogleCredentials json parse error" }
            }
        }

        logger.warn { "GCP 서비스 계정 설정이 없어 기본 자격 증명을 사용합니다." }
        return null
    }

    private fun cleanJsonString(jsonString: String): String {
        return jsonString
            .let { value ->
                if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith('\'') && value.endsWith('\''))) {
                    value.substring(1, value.length - 1)
                } else value
            }
            .let { value ->
                value.replace(Regex("[\\x00-\\x1F\\x7F]"), "")
            }
            .let { value ->
                value.replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\r", "\r")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            }
            .let { value ->
                value.replace("\u0000", "")
                    .replace("\u0001", "")
                    .replace("\u0002", "")
                    .replace("\u0003", "")
                    .replace("\u0004", "")
                    .replace("\u0005", "")
                    .replace("\u0006", "")
                    .replace("\u0007", "")
                    .replace("\u0008", "")
                    .replace("\u000B", "")
                    .replace("\u000C", "")
                    .replace("\u000E", "")
                    .replace("\u000F", "")
                    .replace("\u0010", "")
                    .replace("\u0011", "")
                    .replace("\u0012", "")
                    .replace("\u0013", "")
                    .replace("\u0014", "")
                    .replace("\u0015", "")
                    .replace("\u0016", "")
                    .replace("\u0017", "")
                    .replace("\u0018", "")
                    .replace("\u0019", "")
                    .replace("\u001A", "")
                    .replace("\u001B", "")
                    .replace("\u001C", "")
                    .replace("\u001D", "")
                    .replace("\u001E", "")
                    .replace("\u001F", "")
                    .replace("\u007F", "")
            }
    }
} 
