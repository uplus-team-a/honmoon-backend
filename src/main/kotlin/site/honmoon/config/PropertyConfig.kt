package site.honmoon.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources

@Profile("local")
@Configuration
@PropertySources(
    PropertySource(value = ["classpath:properties/env.properties"], ignoreResourceNotFound = true)
)
class PropertyConfig
