package site.honmoon.mission.dto

import site.honmoon.mission.type.MissionType
import site.honmoon.mission.vo.MissionChoicesVo
import java.time.Instant

data class MissionDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val points: Int,
    val missionType: MissionType,
    val placeId: Long?,
    val question: String?,
    val answer: String?,
    val choices: MissionChoicesVo?,
    val answerExplanation: String?,
    val correctImageUrl: String?,
    val imageUploadInstruction: String?,
    val createdAt: Instant,
    val modifiedAt: Instant,
)

data class MissionCompletionResponse(
    val missionDetail: MissionDetailResponse,
    val userActivity: site.honmoon.activity.dto.UserActivityResponse,
    val message: String = "미션이 성공적으로 완료되었습니다!",
) 
