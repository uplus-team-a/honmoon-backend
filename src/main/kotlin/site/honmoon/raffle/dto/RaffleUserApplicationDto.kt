package site.honmoon.raffle.dto

import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

data class RaffleUserApplicationResponse(
    val id: Long,
    val userId: UUID,
    val raffleProductId: Long,
    val applicationDate: OffsetDateTime,
    val createdAt: Instant,
    val modifiedAt: Instant,
) 

data class RaffleApplyResult(
    val success: Boolean,
    val reasonCode: String? = null,
    val requiredPoints: Int? = null,
    val currentPoints: Int? = null,
    val application: RaffleUserApplicationResponse? = null,
)
