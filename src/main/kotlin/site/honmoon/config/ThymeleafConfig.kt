package site.honmoon.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver

@Configuration
class ThymeleafConfig {
    @Bean
    fun defaultTemplateResolver(): SpringResourceTemplateResolver {
        val resolver = SpringResourceTemplateResolver()
        resolver.prefix = "classpath:/templates/"
        resolver.suffix = ".html"
        resolver.characterEncoding = "UTF-8"
        resolver.isCacheable = true
        resolver.setTemplateMode("HTML")
        return resolver
    }

    @Bean
    fun thymeleafTemplateEngine(resolver: SpringResourceTemplateResolver): SpringTemplateEngine {
        val engine = SpringTemplateEngine()
        engine.setTemplateResolver(resolver)
        return engine
    }
}


