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

@Configuration
class WebMvcConfig(
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: org.springframework.web.servlet.config.annotation.CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(CurrentUserArgumentResolver())
    }
}

class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) && parameter.parameterType == UserPrincipal::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: org.springframework.web.bind.support.WebDataBinderFactory?,
    ): Any? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        val p = auth.principal
        return when (p) {
            is UserPrincipal -> p
            is org.springframework.security.core.userdetails.User -> UserPrincipal(
                subject = p.username,
                email = null,
                name = p.username,
                picture = null,
                provider = "basic",
                roles = p.authorities.map { it.authority }.toSet()
            )

            is String -> UserPrincipal(
                subject = p,
                email = null,
                name = p,
                picture = null,
                provider = "basic",
                roles = auth.authorities.map { it.authority }.toSet()
            )

            else -> null
        }
    }
} 
