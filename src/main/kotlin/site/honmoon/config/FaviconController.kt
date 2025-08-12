package site.honmoon.config

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FaviconController {
    @GetMapping("/favicon.ico")
    fun favicon(): ResponseEntity<Void> {
        // Swagger UI 기본 파비콘으로 리다이렉트
        return ResponseEntity.status(302)
            .header(HttpHeaders.LOCATION, "/swagger-ui/favicon-32x32.png")
            .build()
    }
}


