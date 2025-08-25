package site.honmoon.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import site.honmoon.auth.service.JwtService
import site.honmoon.user.repository.UsersRepository
import java.util.*

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val usersRepository: UsersRepository,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        
        val token = authorizationHeader.substring(7)
        
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response)
            return
        }
        
        val userId = jwtService.extractUserId(token)
        val email = jwtService.extractEmail(token)
        
        if (SecurityContextHolder.getContext().authentication == null) {
            // 사용자 정보 조회 (선택사항)
            val user = try {
                usersRepository.findById(UUID.fromString(userId)).orElse(null)
            } catch (e: Exception) {
                null
            }
            
            val userPrincipal = UserPrincipal(
                subject = userId,
                email = email,
                name = user?.nickname ?: email ?: userId,
                picture = user?.profileImageUrl,
                provider = "jwt",
                roles = setOf("ROLE_USER")
            )
            
            val authToken = UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )
            
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authToken
        }
        
        filterChain.doFilter(request, response)
    }
}
