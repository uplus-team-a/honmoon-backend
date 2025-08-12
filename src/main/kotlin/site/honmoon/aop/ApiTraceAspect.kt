package site.honmoon.aop

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

/**
 * 컨트롤러 단 AOP로 API 호출 로그를 기록한다.
 * 예외도 포착하여 에러 로그에 반영한다.
 */
@Aspect
@Component
class ApiTraceAspect {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val ACTUATOR_URL = "/actuator"
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    fun traceApi(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        var exception: Throwable? = null

        val request = currentHttpRequest() ?: return joinPoint.proceed()
        val response = currentHttpResponse()
        val requestUri = request.requestURI
        if (requestUri.startsWith(ACTUATOR_URL)) {
            return joinPoint.proceed()
        }

        try {
            return joinPoint.proceed()
        } catch (ex: Throwable) {
            exception = ex
            throw ex
        } finally {
            val duration = System.currentTimeMillis() - startTime
            logRequest(request, response, duration, exception)
        }
    }

    private fun currentHttpRequest(): HttpServletRequest? {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return attrs?.request
    }
    private fun currentHttpResponse(): HttpServletResponse? {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return attrs?.response
    }

    private fun logRequest(
        request: HttpServletRequest,
        response: HttpServletResponse?,
        duration: Long,
        exception: Throwable?,
    ) {
        val method = request.method
        val requestUri = request.requestURI
        val queryString = request.queryString?.let { "?$it" } ?: ""

        val status = response?.status ?: 200

        val wrappedReq = getContentCachingRequestWrapper(request)
        val wrappedRes = response?.let { getContentCachingResponseWrapper(it) }

        val requestBody = if (method == "POST" || method == "PUT" || method == "PATCH") {
            val content = wrappedReq.contentAsByteArray
            if (content.isNotEmpty()) String(content, Charsets.UTF_8) else ""
        } else ""

        val responseBody = runCatching {
            val content = wrappedRes?.contentAsByteArray
            if (content != null && content.isNotEmpty()) String(content, Charsets.UTF_8) else ""
        }.getOrElse { "" }

        val fullLog =
            "[ApiTrace] $method $requestUri$queryString " +
                    "| Status=$status " +
                    "| Duration=${duration}ms" +
                    (if (requestBody.isNotBlank()) " | RequestBody=$requestBody" else "") +
                    (if (responseBody.isNotBlank()) " | ResponseBody=$responseBody" else "") +
                    (exception?.let { " | Exception=${it.javaClass.simpleName}" } ?: "")

        if (status < 400)
            log.info { fullLog }
        else
            log.error(exception) { fullLog }
    }

    private fun getContentCachingRequestWrapper(request: HttpServletRequest): ContentCachingRequestWrapper {
        return request as? ContentCachingRequestWrapper ?: ContentCachingRequestWrapper(request)
    }

    private fun getContentCachingResponseWrapper(response: HttpServletResponse): ContentCachingResponseWrapper {
        return response as? ContentCachingResponseWrapper ?: ContentCachingResponseWrapper(response)
    }
}
