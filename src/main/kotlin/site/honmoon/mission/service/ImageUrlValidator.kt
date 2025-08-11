package site.honmoon.mission.service

import org.springframework.stereotype.Component
import java.net.URI
import java.net.URISyntaxException
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.InvalidRequestException

@Component
class ImageUrlValidator {

    private val allowedSchemes = setOf("http", "https")
    private val allowedImageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")

    fun validateImageUrl(imageUrl: String): URI {
        if (imageUrl.isBlank()) {
            throw InvalidRequestException(ErrorCode.IMAGE_URL_EMPTY)
        }

        val uri = try {
            URI(imageUrl)
        } catch (e: URISyntaxException) {
            throw InvalidRequestException(ErrorCode.INVALID_URL_FORMAT, e.message)
        }

        if (uri.scheme !in allowedSchemes) {
            throw InvalidRequestException(ErrorCode.UNSUPPORTED_URL_SCHEME)
        }

        if (uri.host.isNullOrBlank()) {
            throw InvalidRequestException(ErrorCode.INVALID_URL_HOST)
        }

        val path = uri.path?.lowercase() ?: ""
        if (path.contains("..") || path.contains("//")) {
            throw InvalidRequestException(ErrorCode.PATH_TRAVERSAL_DETECTED)
        }

        val extension = path.substringAfterLast(".", "")
        if (extension.isNotEmpty() && extension !in allowedImageExtensions) {
            throw InvalidRequestException(ErrorCode.UNSUPPORTED_IMAGE_EXTENSION)
        }

        return uri
    }
}
