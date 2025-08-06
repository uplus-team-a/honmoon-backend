package site.honmoon.user.entity

import jakarta.persistence.*
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity
import java.util.*

@Entity
@Table(name = "user_summary")
class UserSummary(
    @Column(name = "user_id", nullable = false, unique = true)
    var userId: UUID,
    
    @Column(name = "total_points", nullable = false)
    var totalPoints: Int,
    
    @Column(name = "total_activities", nullable = false)
    var totalActivities: Int
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 