package site.honmoon.auth.service


import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.auth.dto.*
import site.honmoon.auth.entity.EmailAuthToken
import site.honmoon.auth.repository.EmailAuthTokenRepository
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.common.exception.InvalidRequestException
import site.honmoon.user.entity.Users
import site.honmoon.user.repository.UsersRepository
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class EmailAuthService(
    private val emailAuthTokenRepository: EmailAuthTokenRepository,
    private val usersRepository: UsersRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    
    private val secureRandom = SecureRandom()
    
    fun sendSignupEmail(request: EmailAuthRequest, callbackUrl: String?): EmailAuthResponse {
        val existingUser = usersRepository.findByEmail(request.email)
        if (existingUser != null) {
            throw InvalidRequestException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }
        
        val nickname = request.email.substringBefore("@")
        
        emailAuthTokenRepository.markAllUsedByEmail(request.email)
        
        val token = generateSecureToken()
        
        val emailAuthToken = EmailAuthToken(
            email = request.email,
            token = token,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )
        
        emailAuthTokenRepository.save(emailAuthToken)
        
        val baseUrl = callbackUrl ?: "https://www.honmoon.site/auth/verify"
        val verifyLink = if (baseUrl.contains("?")) {
            "$baseUrl&token=$token"
        } else {
            "$baseUrl?token=$token"
        }
        
        emailService.sendMagicLinkHtml(
            email = request.email,
            link = verifyLink,
            purpose = "회원가입",
            name = nickname
        )
        
        return EmailAuthResponse(
            success = true,
            message = "회원가입 인증 이메일이 발송되었습니다. 메일함을 확인해주세요."
        )
    }
    
    fun login(request: LoginRequest): LoginResponse {
        val user = usersRepository.findByEmail(request.email)
            ?: throw EntityNotFoundException(ErrorCode.EMAIL_NOT_REGISTERED)
        
        if (user.isActive != true) {
            throw InvalidRequestException(ErrorCode.ACCOUNT_DEACTIVATED)
        }
        
        if (user.passwordHash == null) {
            throw InvalidRequestException(ErrorCode.PASSWORD_NOT_SET)
        }
        
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidRequestException(ErrorCode.INVALID_EMAIL_OR_PASSWORD)
        }
        
        val token = jwtService.generateToken(user.id.toString(), user.email ?: "")
        
        return LoginResponse(
            success = true,
            userId = user.id,
            token = token,
            message = "로그인이 완료되었습니다",
            email = user.email,
            nickname = user.nickname
        )
    }
    
    fun verifyToken(request: TokenVerifyRequest): TokenVerifyResponse {
        val emailAuthToken = emailAuthTokenRepository.findByTokenAndUsedAndExpiresAtAfter(request.token, false, LocalDateTime.now())
            ?: throw EntityNotFoundException(ErrorCode.EMAIL_AUTH_TOKEN_INVALID)
        
        if (request.nickname.isBlank()) {
            throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "nickname")
        }
        
        if (request.password.isBlank()) {
            throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "password")
        }
        
        return processSignup(emailAuthToken, request.nickname, request.password)
    }
    
    private fun processSignup(emailAuthToken: EmailAuthToken, nickname: String, password: String): TokenVerifyResponse {
        val existingUser = usersRepository.findByEmail(emailAuthToken.email)
        if (existingUser != null) {
            throw InvalidRequestException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }
        
        val hashedPassword = passwordEncoder.encode(password)
        
        val newUser = Users(
            id = UUID.randomUUID(),
            email = emailAuthToken.email,
            nickname = nickname,
            passwordHash = hashedPassword,
            profileImageUrl = "https://storage.googleapis.com/honmoon-bucket/image/honmmon.png"
        )
        
        usersRepository.save(newUser)
        
        emailAuthToken.used = true
        emailAuthTokenRepository.save(emailAuthToken)
        
        val token = jwtService.generateToken(newUser.id.toString(), newUser.email ?: "")
        
        return TokenVerifyResponse(
            success = true,
            userId = newUser.id,
            token = token,
            message = "회원가입이 완료되었습니다",
            email = newUser.email,
            nickname = newUser.nickname
        )
    }
    

    
    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
