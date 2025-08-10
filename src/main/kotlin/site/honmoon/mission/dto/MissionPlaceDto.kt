package site.honmoon.mission.dto

import java.time.Instant

data class MissionPlaceResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val location: String?,
    val image: String?,
    val createdAt: Instant,
    val modifiedAt: Instant
) 