package site.honmoon.raffle.entity

import jakarta.persistence.*
import org.springframework.data.relational.core.mapping.Table
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "raffle_user_application")
class RaffleUserApplication(
    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "raffle_product_id", nullable = false)
    var raffleProductId: Long,

    @Column(name = "application_date", nullable = false)
    var applicationDate: LocalDateTime,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 
