package site.honmoon.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class Response<T>(
    data: T?,
    status: HttpStatus = HttpStatus.OK,
    message: String = "Success",
) : ResponseEntity<Response.ResponseBody<T>>(ResponseBody(data, status.value(), message), status) {

    companion object {
        fun <T> success(data: T, message: String = "Success"): Response<T> {
            return Response(data, HttpStatus.OK, message)
        }

        fun <T> created(data: T, message: String = "Created successfully"): Response<T> {
            return Response(data, HttpStatus.CREATED, message)
        }

        fun <T> noContent(message: String = "No content"): Response<T> {
            return Response(null, HttpStatus.NO_CONTENT, message)
        }

        fun <T> error(message: String, status: HttpStatus = HttpStatus.BAD_REQUEST): Response<T> {
            return Response(null, status, message)
        }
    }

    data class ResponseBody<T>(
        val data: T?,
        val status: Int,
        val message: String,
    )
}
