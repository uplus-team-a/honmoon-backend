package site.honmoon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler
import org.springframework.security.web.firewall.RequestRejectedHandler
import org.springframework.web.servlet.HandlerExceptionResolver
import site.honmoon.auth.security.SecurityExceptionHandlerDelegator
import site.honmoon.auth.security.SecurityRoles
import site.honmoon.auth.security.SessionAuthService
import site.honmoon.auth.security.TokenAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val sessionAuthService: SessionAuthService,
    private val handlerExceptionResolver: HandlerExceptionResolver,
    @Value("\${BASIC_AUTH_USERNAME}") private val basicUsername: String,
    @Value("\${BASIC_AUTH_PASSWORD}") private val basicPassword: String,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val user = User.withUsername(basicUsername)
            .password(passwordEncoder.encode(basicPassword))
            .roles(SecurityRoles.ADMIN, SecurityRoles.USER)
            .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val delegator = SecurityExceptionHandlerDelegator(handlerExceptionResolver)

        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { eh ->
                eh.authenticationEntryPoint(delegator)
                eh.accessDeniedHandler(delegator)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/auth/google/url",
                        "/api/auth/google/callback",
                        "/api/auth/signup/email",
                        "/api/auth/login/email/by-user",
                        "/api/auth/email/callback",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/actuator/health"
                    ).permitAll()
                    .requestMatchers("/api/auth/test-token").hasRole(SecurityRoles.ADMIN)
                    .anyRequest().hasRole(SecurityRoles.USER)
            }
            .httpBasic { }
            .addFilterAfter(
                TokenAuthenticationFilter(sessionAuthService, handlerExceptionResolver),
                SecurityContextHolderFilter::class.java
            )
        return http.build()
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
