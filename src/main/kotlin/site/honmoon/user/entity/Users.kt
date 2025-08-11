package site.honmoon.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.relational.core.mapping.Table
import site.honmoon.common.entity.BaseEntity
import java.util.*

@Entity
@Table(name = "users")
class Users(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,

    @Column(name = "email")
    var email: String?,

    @Column(name = "nickname")
    var nickname: String? = null,

    @Column(name = "total_points", nullable = false)
    var totalPoints: Int = 0,

    @Column(name = "total_activities", nullable = false)
    var totalActivities: Int = 0,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) : BaseEntity()


