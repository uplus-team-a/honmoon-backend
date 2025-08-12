package site.honmoon.auth.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.servlet.HandlerExceptionResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Spring Security의 인증/인가 예외를 Spring MVC의 HandlerExceptionResolver로 위임한다.
 * 이를 통해 @RestControllerAdvice 에서 일관된 에러 응답을 생성할 수 있다.
 */
class SecurityExceptionHandlerDelegator(
    private val handlerExceptionResolver: HandlerExceptionResolver,
) : AuthenticationEntryPoint, AccessDeniedHandler {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        handlerExceptionResolver.resolveException(request, response, null, authException)
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        handlerExceptionResolver.resolveException(request, response, null, accessDeniedException)
    }
}


