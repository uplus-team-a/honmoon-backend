package site.honmoon.common.aop

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.beans.TypeMismatchException
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import site.honmoon.common.ErrorCode
import site.honmoon.common.ErrorResponse
import site.honmoon.common.exception.CommonException
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.common.exception.InvalidRequestException

@Hidden
@RestControllerAdvice
class ControllerExceptionAdvice(
    private val messageSourceAccessor: MessageSourceAccessor,
) : ResponseEntityExceptionHandler() {
    val log: KLogger = KotlinLogging.logger {}

    @ExceptionHandler(CommonException::class)
    fun commonException(e: CommonException): ResponseEntity<ErrorResponse> {
        log.warn(e) { "[CommonException] Code: ${e.code}, Message: ${e.message}" }
        return ResponseEntity.status(e.status.toHttpStatus())
            .body(ErrorResponse(e.code, e.message ?: "요청한 리소스를 찾을 수 없습니다."))
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun entityNotFoundException(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn(e) { "[EntityNotFoundException] Code: ${e.code}, Message: ${e.message}" }
        return ResponseEntity.status(e.status.toHttpStatus())
            .body(ErrorResponse(e.code, e.message ?: "요청한 엔티티를 찾을 수 없습니다."))
    }

    private fun makeErrorMessage(e: Exception): String {
        if (e is IllegalArgumentException)
            return messageSourceAccessor.getMessage(e.javaClass.simpleName, "잘못된 요청입니다.")
        if (e is MethodArgumentTypeMismatchException)
            return "잘못된 요청입니다. (${e.name}=${e.value})"

        log.error(e) { "[makeErrorMessage] ${e.message}" }
        return messageSourceAccessor.getMessage(e.javaClass.simpleName, "서비스에 문제가 발생했습니다.")
    }

    @ExceptionHandler(Exception::class)
    fun unknownException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(ErrorCode.UNKNOWN_ERROR.status.toHttpStatus())
            .body(
                ErrorResponse(
                    ErrorCode.ILLEGAL_REQUEST.code,
                    makeErrorMessage(e)
                )
            )
    }

    @ExceptionHandler(AuthenticationException::class)
    fun authenticationException(e: AuthenticationException): ResponseEntity<ErrorResponse> {
        log.warn(e) { "[AuthenticationException] ${e.message}" }
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "인증이 필요합니다."))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun accessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        log.warn(e) { "[AccessDeniedException] ${e.message}" }
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), "접근 권한이 없습니다."))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(ErrorCode.ILLEGAL_REQUEST.status.toHttpStatus())
            .body(
                ErrorResponse(
                    ErrorCode.ILLEGAL_REQUEST.code,
                    makeErrorMessage(e)
                )
            )
    }

    @ExceptionHandler(InvalidRequestException::class)
    fun invalidRequestException(e: InvalidRequestException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(e.status.toHttpStatus())
            .body(
                ErrorResponse(
                    e.code,
                    e.message ?: "잘못된 요청입니다."
                )
            )
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(status).body(
            ErrorResponse(
                ErrorCode.ILLEGAL_REQUEST.code,
                makeErrorMessage(ex)
            )
        )
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(status).body(
            ErrorResponse(
                ErrorCode.ILLEGAL_REQUEST.code,
                "JSON parse error, Invalid Request Body"
            )
        )
    }

    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    ErrorCode.NO_HANDLER.code,
                    makeErrorMessage(ex)
                )
            )
    }

    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(status).body(
            ErrorResponse(
                ErrorCode.ILLEGAL_REQUEST.code,
                makeErrorMessage(ex)
            )
        )
    }

    override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        log.warn(ex) { "[handleExceptionInternal] $body" }
        return super.handleExceptionInternal(
            ex,
            ErrorResponse(statusCode.value(), makeErrorMessage(ex)),
            headers,
            statusCode,
            request
        )
    }
}
