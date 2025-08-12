package site.honmoon.auth.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * DB에 저장된 앱 세션 토큰(Bearer)을 검증하고 SecurityContext에 `UserPrincipal`을 설정한다.
 * Basic 인증은 건드리지 않고, `Authorization: Bearer <token>` 헤더가 있을 때만 동작한다.
 */
class TokenAuthenticationFilter(
    private val sessionAuthService: SessionAuthService,
    private val handlerExceptionResolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {

    private val logger = KotlinLogging.logger {}

    /**
     * 요청 헤더의 Bearer 토큰을 조회해 세션을 검증하고 인증 컨텍스트를 설정한다.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            if (authHeader == null) {
                logger.debug { "[TokenAuth] Authorization header is missing" }

            } else if (!authHeader.startsWith("Bearer", ignoreCase = true)) {
                logger.debug { "[TokenAuth] Authorization header present but not Bearer (startsWith=Bearer? false)" }

            } else {
                val token = authHeader.substringAfter("Bearer ", "").trim()

                if (token.isNotBlank()) {
                    val principal = sessionAuthService.authenticate(token)
                    if (principal != null) {
                        val authorities: List<GrantedAuthority> = principal.roles.map { SimpleGrantedAuthority(it) }
                        val authentication = UsernamePasswordAuthenticationToken(principal, token, authorities)
                        SecurityContextHolder.getContext().authentication = authentication
                    } else {
                        logger.warn { "[TokenAuth] Token authentication failed (invalid or expired token) token=${token}" }
                    }
                } else {
                    logger.debug { "[TokenAuth] Bearer token is blank after parsing Authorization header" }
                }
            }
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.error(ex) { "[TokenAuth] Exception while authenticating token: ${ex.javaClass.simpleName}: ${ex.message}" }
            handlerExceptionResolver.resolveException(request, response, null, ex)
        }
    }
} 
