package site.honmoon.auth.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "app_session")
@EntityListeners(AuditingEntityListener::class)
data class AuthSession(
    @Id
    @Column(name = "token", nullable = false, length = 64)
    var token: String = "",

    @Column(name = "subject", nullable = false, length = 255)
    var subject: String,

    @Column(name = "email", length = 255)
    var email: String? = null,

    @Column(name = "name", length = 255)
    var name: String? = null,

    @Column(name = "picture", length = 500)
    var picture: String? = null,

    @Column(name = "provider", nullable = false, length = 50)
    var provider: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
)


