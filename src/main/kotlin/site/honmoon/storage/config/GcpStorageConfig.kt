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

    @Value("\${GCP_SERVICE_ACCOUNT_JSON:\${SPRING_GCP_CREDENTIALS_JSON:}}")
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
            // 1차: 원본 그대로 시도
            try {
                return GoogleCredentials.fromStream(ByteArrayInputStream(serviceAccountJson.trim().toByteArray()))
            } catch (_: Exception) {
                // 2차: 실제 개행 문자를 JSON 호환 "\\n"으로 변환 후 재시도
                try {
                    val normalized = normalizeNewlinesForJson(serviceAccountJson)
                    return GoogleCredentials.fromStream(ByteArrayInputStream(normalized.toByteArray()))
                } catch (e: Exception) {
                    logger.warn { "GoogleCredentials json parse error" }
                }
            }
        }

        logger.warn { "GCP 서비스 계정 설정이 없어 기본 자격 증명을 사용합니다" }
        return null
    }

    private fun normalizeNewlinesForJson(value: String): String {
        val trimmed = value.trim()
        // properties 로더가 \n 을 실제 개행으로 변환했을 가능성을 고려해 실제 개행을 JSON 호환 \\n 으로 치환
        return trimmed
            .replace("\r\n", "\\n")
            .replace("\n", "\\n")
    }
} 
