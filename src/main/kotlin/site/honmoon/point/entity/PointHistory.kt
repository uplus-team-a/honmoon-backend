package site.honmoon.point.entity

import jakarta.persistence.*
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity
import java.util.*

@Entity
@Table(name = "point_history")
class PointHistory(
    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "points", nullable = false)
    var points: Int,

    @Column(name = "description", nullable = false)
    var description: String,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 
