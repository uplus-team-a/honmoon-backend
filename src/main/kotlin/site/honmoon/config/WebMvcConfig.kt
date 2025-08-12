package site.honmoon.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
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
 * Basic 인증의 Principal도 통일된 `UserPrincipal`로 변환한다.
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

            is org.springframework.security.core.userdetails.User -> {
                loadPrincipalFromRepository(p.username, roles)
                    ?: UserPrincipal(
                        subject = p.username,
                        email = p.username.takeIf { it.contains("@") },
                        name = p.username,
                        picture = null,
                        provider = "basic",
                        roles = roles,
                    )
            }

            is String -> {
                loadPrincipalFromRepository(p, roles)
                    ?: UserPrincipal(
                        subject = p,
                        email = p.takeIf { it.contains("@") },
                        name = p,
                        picture = null,
                        provider = "basic",
                        roles = roles,
                    )
            }

            else -> null
        }
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
