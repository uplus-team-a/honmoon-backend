package site.honmoon.auth.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "magic_link_token")
@EntityListeners(AuditingEntityListener::class)
data class MagicLinkToken(
    @Id
    @Column(name = "token", nullable = false, length = 64)
    var token: String = "",

    @Column(name = "email", nullable = false, length = 255)
    var email: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
)


