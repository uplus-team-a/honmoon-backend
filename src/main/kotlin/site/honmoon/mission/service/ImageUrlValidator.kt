package site.honmoon.mission.service

import org.springframework.stereotype.Component
import java.net.URI
import java.net.URISyntaxException

@Component
class ImageUrlValidator {
    
    private val allowedSchemes = setOf("http", "https")
    private val allowedImageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    
    fun validateImageUrl(imageUrl: String): URI {
        if (imageUrl.isBlank()) {
            throw IllegalArgumentException("Image URL cannot be empty")
        }
        
        val uri = try {
            URI(imageUrl)
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException("Invalid URL format: ${e.message}")
        }
        
        if (uri.scheme !in allowedSchemes) {
            throw IllegalArgumentException("Only HTTP and HTTPS URLs are allowed")
        }
        
        if (uri.host.isNullOrBlank()) {
            throw IllegalArgumentException("URL must have a valid host")
        }
        
        val path = uri.path?.lowercase() ?: ""
        if (path.contains("..") || path.contains("//")) {
            throw IllegalArgumentException("Path traversal attempts are not allowed")
        }
        
        val extension = path.substringAfterLast(".", "")
        if (extension.isNotEmpty() && extension !in allowedImageExtensions) {
            throw IllegalArgumentException("Only image files are allowed")
        }
        
        return uri
    }
}