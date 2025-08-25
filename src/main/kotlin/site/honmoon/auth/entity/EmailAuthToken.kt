package site.honmoon.auth.entity

import jakarta.persistence.*
import site.honmoon.common.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "email_auth_tokens")
class EmailAuthToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0,

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "token", nullable = false, unique = true)
    var token: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(name = "used", nullable = false)
    var used: Boolean = false,
) : BaseEntity()
