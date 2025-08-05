package site.honmoon.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> success(data: T, message: String = "Success"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    message = message,
                    data = data
                )
            )
        }

        fun <T> created(data: T, message: String = "Created successfully"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    status = HttpStatus.CREATED.value(),
                    message = message,
                    data = data
                )
            )
        }

        fun <T> noContent(message: String = "No content"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse(
                    status = HttpStatus.NO_CONTENT.value(),
                    message = message,
                    data = null
                )
            )
        }
    }
}

