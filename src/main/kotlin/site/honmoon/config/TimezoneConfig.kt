package site.honmoon.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.util.*

/**
 * 애플리케이션 전역 타임존을 UTC로 고정한다.
 */
@Configuration
class TimezoneConfig {
    @PostConstruct
    fun setUtcTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        System.setProperty("user.timezone", "UTC")
    }
}


