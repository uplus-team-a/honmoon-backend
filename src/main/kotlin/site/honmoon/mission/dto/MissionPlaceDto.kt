package site.honmoon.mission.dto

import site.honmoon.mission.dto.MissionDetailResponse
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

data class MissionPlaceWithMissionsResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val location: String?,
    val image: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val missions: List<MissionDetailResponse>,
)
