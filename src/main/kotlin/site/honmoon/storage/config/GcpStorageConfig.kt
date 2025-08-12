package site.honmoon.storage.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.util.*

@Configuration
class GcpStorageConfig {

    private val logger = KotlinLogging.logger {}

    @Value("\${GCP_SERVICE_ACCOUNT_JSON:}")
    private lateinit var serviceAccountJson: String

    @Value("\${GCP_PROJECT_ID:}")
    private var projectId: String? = null

    @Bean
    fun storage(): Storage {
        val credential: GoogleCredentials? = resolveCredentials()

        if (credential != null || !projectId.isNullOrBlank()) {
            val builder = StorageOptions.newBuilder()

            if (credential != null) {
                val scoped = if (credential.createScopedRequired())
                    credential.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                else
                    credential
                builder.setCredentials(scoped)
            }

            projectId?.takeIf { it.isNotBlank() }?.let { builder.setProjectId(it) }

            return builder.build().service
        }

        logger.warn { "명시적 GCP 설정이 없어 Application Default Credentials로 동작합니다." }
        return StorageOptions.getDefaultInstance().service
    }

    private fun resolveCredentials(): GoogleCredentials? {
        if (!(this::serviceAccountJson.isInitialized) || serviceAccountJson.isBlank()) return null

        return try {
            val raw = serviceAccountJson.trim()
            val json = if (raw.startsWith("{")) {
                raw
            } else {
                String(Base64.getDecoder().decode(raw), Charsets.UTF_8)
            }
            GoogleCredentials.fromStream(ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)))
        } catch (e: Exception) {
            logger.warn { "GoogleCredentials 로드 실패: ${e.message}. ADC로 폴백합니다." }
            null
        }
    }
}
