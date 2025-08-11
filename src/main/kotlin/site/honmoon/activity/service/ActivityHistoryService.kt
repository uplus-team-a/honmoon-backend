package site.honmoon.activity.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.activity.dto.UserActivityResponse
import site.honmoon.activity.entity.UserActivity
import site.honmoon.activity.repository.UserActivityRepository
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.DuplicateResourceException
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.mission.entity.MissionDetail
import site.honmoon.mission.repository.MissionDetailRepository
import site.honmoon.mission.service.FallbackAIService
import site.honmoon.mission.type.MissionType
import site.honmoon.point.service.PointHistoryService
import site.honmoon.user.repository.UsersRepository
import java.util.*

/**
 * 사용자 활동 기록과 미션 제출을 처리하는 서비스
 */
@Service
@Transactional(readOnly = true)
class UserActivityService(
    private val userActivityRepository: UserActivityRepository,
    private val usersRepository: UsersRepository,
    private val missionDetailRepository: MissionDetailRepository,
    private val pointHistoryService: PointHistoryService,
    private val fallbackAIService: FallbackAIService,
) {
    /**
     * 사용자 활동 단건을 조회한다.
     */
    fun getUserActivity(id: Long): UserActivityResponse {
        val userActivity = userActivityRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException(ErrorCode.ACTIVITY_NOT_FOUND, "ID: $id")
        return UserActivityResponse(
            id = userActivity.id,
            userId = userActivity.userId,
            placeId = userActivity.placeId,
            missionId = userActivity.missionId,
            description = userActivity.description,
            isCorrect = userActivity.isCorrect,
            isCompleted = userActivity.isCompleted,
            pointsEarned = userActivity.pointsEarned,
            textAnswer = userActivity.textAnswer,
            selectedChoiceIndex = userActivity.selectedChoiceIndex,
            uploadedImageUrl = userActivity.uploadedImageUrl,
            createdAt = userActivity.createdAt,
            modifiedAt = userActivity.modifiedAt
        )
    }

    /**
     * 특정 사용자의 활동 내역을 조회한다.
     */
    fun getUserActivityHistory(userId: UUID): List<UserActivityResponse> {
        return userActivityRepository.findByUserId(userId).map { userActivity ->
            UserActivityResponse(
                id = userActivity.id,
                userId = userActivity.userId,
                placeId = userActivity.placeId,
                missionId = userActivity.missionId,
                description = userActivity.description,
                isCorrect = userActivity.isCorrect,
                isCompleted = userActivity.isCompleted,
                pointsEarned = userActivity.pointsEarned,
                textAnswer = userActivity.textAnswer,
                selectedChoiceIndex = userActivity.selectedChoiceIndex,
                uploadedImageUrl = userActivity.uploadedImageUrl,
                createdAt = userActivity.createdAt,
                modifiedAt = userActivity.modifiedAt
            )
        }
    }

    /**
     * 특정 장소의 활동 내역을 조회한다.
     */
    fun getActivityHistoryByPlace(placeId: Long): List<UserActivityResponse> {
        return userActivityRepository.findByPlaceId(placeId).map { userActivity ->
            UserActivityResponse(
                id = userActivity.id,
                userId = userActivity.userId,
                placeId = userActivity.placeId,
                missionId = userActivity.missionId,
                description = userActivity.description,
                isCorrect = userActivity.isCorrect,
                isCompleted = userActivity.isCompleted,
                pointsEarned = userActivity.pointsEarned,
                textAnswer = userActivity.textAnswer,
                selectedChoiceIndex = userActivity.selectedChoiceIndex,
                uploadedImageUrl = userActivity.uploadedImageUrl,
                createdAt = userActivity.createdAt,
                modifiedAt = userActivity.modifiedAt
            )
        }
    }

    /**
     * 자유 활동을 기록하고 사용자 요약의 활동 수를 증가시킨다.
     */
    @Transactional
    fun createActivity(userId: UUID, placeId: Long, description: String): UserActivityResponse {
        userActivityRepository.findByUserIdAndPlaceId(userId, placeId)?.let {
            throw DuplicateResourceException(
                ErrorCode.DUPLICATE_ACTIVITY,
                "User $userId already has activity for place $placeId"
            )
        }

        val userActivity = UserActivity(
            userId = userId,
            placeId = placeId,
            description = description,
            isCompleted = true
        )
        val savedActivity = userActivityRepository.save(userActivity)

        val user = usersRepository.findById(userId).orElse(null)
        user?.let {
            it.totalActivities += 1
            usersRepository.save(it)
        }

        return UserActivityResponse(
            id = savedActivity.id,
            userId = savedActivity.userId,
            placeId = savedActivity.placeId,
            missionId = savedActivity.missionId,
            description = savedActivity.description,
            isCorrect = savedActivity.isCorrect,
            isCompleted = savedActivity.isCompleted,
            pointsEarned = savedActivity.pointsEarned,
            textAnswer = savedActivity.textAnswer,
            selectedChoiceIndex = savedActivity.selectedChoiceIndex,
            uploadedImageUrl = savedActivity.uploadedImageUrl,
            createdAt = savedActivity.createdAt,
            modifiedAt = savedActivity.modifiedAt
        )
    }

    /**
     * 사용자의 최근 활동을 조회한다.
     */
    fun getUserRecentActivity(userId: UUID, limit: Int): List<UserActivityResponse> {
        return userActivityRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .take(limit)
            .map { userActivity ->
                UserActivityResponse(
                    id = userActivity.id,
                    userId = userActivity.userId,
                    placeId = userActivity.placeId,
                    missionId = userActivity.missionId,
                    description = userActivity.description,
                    isCorrect = userActivity.isCorrect,
                    isCompleted = userActivity.isCompleted,
                    pointsEarned = userActivity.pointsEarned,
                    textAnswer = userActivity.textAnswer,
                    selectedChoiceIndex = userActivity.selectedChoiceIndex,
                    uploadedImageUrl = userActivity.uploadedImageUrl,
                    createdAt = userActivity.createdAt,
                    modifiedAt = userActivity.modifiedAt
                )
            }
    }

    /**
     * 미션 퀴즈 답변을 제출한다. 정답 시 포인트를 적립하고 사용자 요약의 활동 수를 증가시킨다.
     */
    @Transactional
    fun submitQuizAnswer(
        missionId: Long,
        userId: UUID,
        textAnswer: String? = null,
        selectedChoiceIndex: Int? = null,
        uploadedImageUrl: String? = null,
    ): UserActivityResponse {
        val missionDetail = missionDetailRepository.findByIdOrNull(missionId)
            ?: throw EntityNotFoundException(ErrorCode.MISSION_NOT_FOUND, "Mission ID: $missionId")

        val placeId = missionDetail.placeId
            ?: throw EntityNotFoundException(ErrorCode.PLACE_NOT_FOUND, "Mission $missionId has no place")

        userActivityRepository.findByUserIdAndPlaceId(userId, placeId)?.let {
            throw DuplicateResourceException(
                ErrorCode.DUPLICATE_ACTIVITY,
                "User $userId already has activity for place $placeId"
            )
        }

        validateQuizAnswer(missionDetail, textAnswer, selectedChoiceIndex, uploadedImageUrl)
        val isCorrect = checkAnswer(missionDetail, textAnswer, selectedChoiceIndex, uploadedImageUrl)

        val pointsToGrant = if (isCorrect) missionDetail.points else 0
        val userActivity = UserActivity(
            userId = userId,
            placeId = placeId,
            missionId = missionId,
            isCompleted = true,
            pointsEarned = pointsToGrant,
            textAnswer = textAnswer,
            selectedChoiceIndex = selectedChoiceIndex,
            uploadedImageUrl = uploadedImageUrl,
            isCorrect = isCorrect
        )
        val savedActivity = userActivityRepository.save(userActivity)

        if (pointsToGrant > 0) {
            pointHistoryService.earnPointsFromQuiz(userId, missionId, pointsToGrant)
        }
        val user = usersRepository.findById(userId).orElse(null)
        user?.let {
            it.totalActivities += 1
            usersRepository.save(it)
        }

        return UserActivityResponse(
            id = savedActivity.id,
            userId = savedActivity.userId,
            placeId = savedActivity.placeId,
            missionId = savedActivity.missionId,
            description = savedActivity.description,
            isCorrect = savedActivity.isCorrect,
            isCompleted = savedActivity.isCompleted,
            pointsEarned = savedActivity.pointsEarned,
            textAnswer = savedActivity.textAnswer,
            selectedChoiceIndex = savedActivity.selectedChoiceIndex,
            uploadedImageUrl = savedActivity.uploadedImageUrl,
            createdAt = savedActivity.createdAt,
            modifiedAt = savedActivity.modifiedAt
        )
    }

    private fun validateQuizAnswer(
        missionDetail: MissionDetail,
        textAnswer: String?,
        selectedChoiceIndex: Int?,
        uploadedImageUrl: String?,
    ) {
        when (missionDetail.missionType) {
            MissionType.QUIZ_MULTIPLE_CHOICE -> {
                requireNotNull(selectedChoiceIndex) { "4지선다 퀴즈는 선택지 인덱스가 필수입니다." }
                requireNotNull(missionDetail.choices) { "4지선다 퀴즈는 선택지가 필수입니다." }
                require(selectedChoiceIndex in 0..(missionDetail.choices?.choices?.size?.minus(1) ?: 0)) {
                    "선택지 인덱스가 유효하지 않습니다."
                }
            }

            MissionType.QUIZ_TEXT_INPUT -> {
                requireNotNull(textAnswer) { "텍스트 입력 퀴즈는 텍스트 답변이 필수입니다." }
                require(textAnswer.isNotBlank()) { "텍스트 답변이 비어있을 수 없습니다." }
            }

            MissionType.QUIZ_IMAGE_UPLOAD -> {
                requireNotNull(uploadedImageUrl) { "이미지 업로드 퀴즈는 업로드된 이미지 URL이 필수입니다." }
            }

            else -> {
            }
        }
    }

    private fun checkAnswer(
        missionDetail: MissionDetail,
        textAnswer: String?,
        selectedChoiceIndex: Int?,
        uploadedImageUrl: String?,
    ): Boolean {
        return when (missionDetail.missionType) {
            MissionType.QUIZ_MULTIPLE_CHOICE -> {
                val correctChoiceIndex = missionDetail.choices?.choices?.indexOf(missionDetail.answer)
                selectedChoiceIndex == correctChoiceIndex
            }

            MissionType.QUIZ_TEXT_INPUT -> {
                textAnswer?.trim()?.equals(missionDetail.answer?.trim(), ignoreCase = true) ?: false
            }

            MissionType.QUIZ_IMAGE_UPLOAD -> {
                val imageUrl = uploadedImageUrl ?: return false
                val analysis = fallbackAIService.analyzeImage(imageUrl)
                val result = fallbackAIService.checkImageAnswer(missionDetail, analysis.extractedText)
                result.isCorrect && result.confidence >= 0.5
            }

            else -> false
        }
    }
} 
