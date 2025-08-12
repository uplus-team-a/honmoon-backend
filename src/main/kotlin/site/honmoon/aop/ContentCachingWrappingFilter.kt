package site.honmoon.aop

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

/**
 * 요청/응답을 ContentCaching 래퍼로 감싼다. 로깅은 AOP 에서 수행한다.
 */
@Component
class ContentCachingWrappingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val wrappedRequest = request as? ContentCachingRequestWrapper ?: ContentCachingRequestWrapper(request)
        val wrappedResponse = response as? ContentCachingResponseWrapper ?: ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            wrappedResponse.copyBodyToResponse()
        }
    }
}
