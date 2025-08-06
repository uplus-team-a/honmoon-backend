package site.honmoon.raffle.entity

import jakarta.persistence.*
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity

@Entity
@Table(name = "raffle_product")
class RaffleProduct(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "point_cost", nullable = false)
    var pointCost: Int = 100,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 