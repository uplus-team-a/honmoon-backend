package site.honmoon.mission.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.activity.service.UserActivityService
import site.honmoon.mission.dto.MissionCompletionResponse
import site.honmoon.mission.dto.MissionDetailResponse
import site.honmoon.mission.repository.MissionDetailRepository
import site.honmoon.user.repository.UsersRepository
import java.util.*

/**
 * 미션 상세 정보를 제공하는 서비스
 */
@Service
@Transactional(readOnly = true)
class MissionDetailService(
    private val missionDetailRepository: MissionDetailRepository,
    private val userActivityService: UserActivityService,
    private val usersRepository: UsersRepository,
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
            placeId = mission.placeId,
            question = mission.question,
            answer = mission.answer,
            choices = mission.choices,
            answerExplanation = mission.answerExplanation,
            correctImageUrl = mission.correctImageUrl,
            imageUploadInstruction = mission.imageUploadInstruction,
            createdAt = mission.createdAt,
            modifiedAt = mission.modifiedAt,
        )
    }

    /**
     * 미션을 무조건 정답으로 처리하고 미션 상세정보와 함께 반환한다.
     */
    @Transactional
    fun completeMissionDirectly(missionId: Long, userId: UUID): MissionCompletionResponse {
        val missionDetail = getMissionDetail(missionId)
        
        val userActivity = userActivityService.submitQuizAnswer(
            missionId = missionId,
            userId = userId,
            textAnswer = "자동 완료",
            selectedChoiceIndex = null,
            uploadedImageUrl = null,
            forceCorrect = true
        )
        
        // 사용자 포인트 및 활동 수 업데이트 (이미 UserActivityService에서 처리되지만 명시적으로 처리)
        val user = usersRepository.findById(userId).orElse(null)
        user?.let {
            if (userActivity.pointsEarned > 0) {
                it.totalPoints += userActivity.pointsEarned
            }
            it.totalActivities += 1
            usersRepository.save(it)
        }
        
        return MissionCompletionResponse(
            missionDetail = missionDetail,
            userActivity = userActivity
        )
    }
}