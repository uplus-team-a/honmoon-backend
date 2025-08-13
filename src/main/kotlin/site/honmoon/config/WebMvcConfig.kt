package site.honmoon.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.user.repository.UsersRepository
import java.util.*

@Configuration
class WebMvcConfig(
    private val usersRepository: UsersRepository,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: org.springframework.web.servlet.config.annotation.CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization", "Content-Type")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addArgumentResolvers(
        resolvers: MutableList<HandlerMethodArgumentResolver>,
    ) {
        resolvers.add(CurrentUserArgumentResolver(usersRepository))
    }
}

/**
 * 컨트롤러 파라미터의 `@CurrentUser UserPrincipal`을 SecurityContext에서 주입한다.
 * Basic 인증의 username은 `user_id`(UUID) 여야 하며, DB 조회 결과가 없으면 401을 반환한다.
 */
class CurrentUserArgumentResolver(
    private val usersRepository: UsersRepository,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: org.springframework.web.bind.support.WebDataBinderFactory?,
    ): Any? {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null)
            return null

        val p = auth.principal
        val roles = auth.authorities.map { it.authority }.toSet()

        return when (p) {
            is UserPrincipal -> p

            is org.springframework.security.core.userdetails.User -> requireAndLoadPrincipal(p.username, roles)

            is String -> requireAndLoadPrincipal(p, roles)

            else -> null
        }
    }

    private fun requireAndLoadPrincipal(subject: String, roles: Set<String>): UserPrincipal {
        val uuid = runCatching { UUID.fromString(subject) }.getOrNull()
            ?: throw org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효한 사용자 토큰이 필요합니다.")

        val user = usersRepository.findById(uuid).orElse(null)
            ?: throw org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다.")

        return UserPrincipal(
            subject = user.id.toString(),
            email = user.email,
            name = user.nickname ?: user.email ?: user.id.toString(),
            picture = user.profileImageUrl,
            provider = "basic",
            roles = roles,
        )
    }

    private fun loadPrincipalFromRepository(subjectOrEmail: String, roles: Set<String>): UserPrincipal? {
        val byId = runCatching { UUID.fromString(subjectOrEmail) }.getOrNull()
            ?.let { uuid -> usersRepository.findById(uuid).orElse(null) }

        val user = byId ?: usersRepository.findByEmail(subjectOrEmail)

        return user?.let {
            UserPrincipal(
                subject = it.id.toString(),
                email = it.email,
                name = it.nickname ?: it.email ?: it.id.toString(),
                picture = it.profileImageUrl,
                provider = "basic",
                roles = roles,
            )
        }
    }
} 
