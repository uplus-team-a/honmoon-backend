package site.honmoon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler
import org.springframework.security.web.firewall.RequestRejectedHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.HandlerExceptionResolver
import site.honmoon.auth.security.SecurityExceptionHandlerDelegator
import site.honmoon.auth.security.SecurityRoles


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val handlerExceptionResolver: HandlerExceptionResolver,
    @Value("\${BASIC_AUTH_PASSWORD:jiwondev}") private val basicPassword: String,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val encoded = passwordEncoder.encode(basicPassword)
        return UserDetailsService { username: String ->
            // 모든 username(UUID 예상)을 허용하고 고정 비밀번호로 검증
            User.withUsername(username)
                .password(encoded)
                .roles(SecurityRoles.USER)
                .build()
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val delegator = SecurityExceptionHandlerDelegator(handlerExceptionResolver)

        http
            .cors { }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { eh ->
                eh.authenticationEntryPoint(delegator)
                eh.accessDeniedHandler(delegator)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/actuator/health",
                        "/favicon.ico"
                    ).permitAll()
                    .anyRequest().hasRole(SecurityRoles.USER)
            }
            .httpBasic { }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.exposedHeaders = listOf("Authorization", "Content-Type")
        config.allowCredentials = true
        config.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun requestRejectedHandler(): RequestRejectedHandler {
        /**
         * 잘못된 URl 문법으로 요청하는 경우 SpringSecurity.StrictHttpFirewall 에서 RequestRejectedException 예외를 던진다.
         * 이 예외는 SpringMVC exceptionResolver 에 잡히지 않으므로, RequestRejectedHandler() 를 추가하여 400을 반환하게 수정
         */
        return HttpStatusRequestRejectedHandler()
    }
} 
