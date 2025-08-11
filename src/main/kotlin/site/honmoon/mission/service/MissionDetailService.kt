package site.honmoon.mission.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.mission.dto.MissionDetailResponse
import site.honmoon.mission.repository.MissionDetailRepository

/**
 * 미션 상세 정보를 제공하는 서비스
 */
@Service
@Transactional(readOnly = true)
class MissionDetailService(
    private val missionDetailRepository: MissionDetailRepository,
) {
    /**
     * 미션 상세 정보를 조회한다.
     */
    fun getMissionDetail(id: Long): MissionDetailResponse {
        val mission = missionDetailRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException(ErrorCode.MISSION_NOT_FOUND, "ID: $id")

        return MissionDetailResponse(
            id = mission.id,
            title = mission.title,
            description = mission.description,
            points = mission.points,
            missionType = mission.missionType,
            question = mission.question,
            answer = mission.answer?.answer,
            choices = mission.choices,
            answerExplanation = mission.answer?.explanation,
            correctImageUrl = mission.imageUpload?.correctImageUrl,
            imageUploadInstruction = mission.imageUpload?.instruction,
            createdAt = mission.createdAt,
            modifiedAt = mission.modifiedAt,
        )
    }
}