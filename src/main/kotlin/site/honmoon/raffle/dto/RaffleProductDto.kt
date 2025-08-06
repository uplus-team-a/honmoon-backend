package site.honmoon.raffle.dto

import java.time.Instant

data class RaffleProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val createdAt: Instant,
    val modifiedAt: Instant
) 