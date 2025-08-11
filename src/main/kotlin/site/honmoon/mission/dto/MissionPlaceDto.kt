package site.honmoon.mission.dto

import java.time.Instant

data class MissionPlaceResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val location: String?,
    val image: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: Instant,
    val modifiedAt: Instant,
) 
