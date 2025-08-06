package site.honmoon.mission.dto

import site.honmoon.mission.type.MissionType

data class MissionSummaryResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val points: Int,
    val missionType: MissionType
) 