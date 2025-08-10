package site.honmoon

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.cloud.openfeign.EnableFeignClients


object HonMoonApplicationDefault {
    val properties = mapOf(
        "spring.application.name" to "api",
        "spring.profiles.active" to "local",
        "logging.level.root" to "info"
    )
}

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients
class HonMoonApplication : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder =
        builder
            .properties(HonMoonApplicationDefault.properties)
            .sources(HonMoonApplication::class.java)
}

fun main(args: Array<String>) {
    logger.info {
        """
        Swagger UI: https://honmoon-api.site/swagger-ui.html
        OpenAPI Docs (JSON): https://honmoon-api.site/v3/api-docs
        """.trimIndent()
    }

    SpringApplicationBuilder()
        .properties(HonMoonApplicationDefault.properties)
        .sources(HonMoonApplication::class.java)
        .run(*args)
}

val logger: KLogger = KotlinLogging.logger {}
