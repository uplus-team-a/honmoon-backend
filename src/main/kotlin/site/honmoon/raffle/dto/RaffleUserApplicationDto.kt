package site.honmoon.raffle.dto

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

data class RaffleUserApplicationResponse(
    val id: Long,
    val userId: UUID,
    val raffleProductId: Long,
    val applicationDate: LocalDateTime,
    val createdAt: Instant,
    val modifiedAt: Instant,
) 
