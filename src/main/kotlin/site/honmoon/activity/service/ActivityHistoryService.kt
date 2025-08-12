package site.honmoon.activity.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.activity.dto.UserActivityResponse
import site.honmoon.activity.entity.UserActivity
import site.honmoon.activity.repository.UserActivityRepository
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.common.exception.InvalidRequestException
import site.honmoon.mission.dto.AnswerCheckResult
import site.honmoon.mission.entity.MissionDetail
import site.honmoon.mission.repository.MissionDetailRepository
import site.honmoon.mission.service.FallbackAIService
import site.honmoon.mission.service.ImageUrlValidator
import site.honmoon.mission.type.MissionType.*
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
    private val imageUrlValidator: ImageUrlValidator,
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
        userActivityRepository.findByUserIdAndPlaceId(userId, placeId)?.let { existing ->
            return UserActivityResponse(
                id = existing.id,
                userId = existing.userId,
                placeId = existing.placeId,
                missionId = existing.missionId,
                description = existing.description,
                isCorrect = existing.isCorrect,
                isCompleted = existing.isCompleted,
                pointsEarned = existing.pointsEarned,
                textAnswer = existing.textAnswer,
                selectedChoiceIndex = existing.selectedChoiceIndex,
                uploadedImageUrl = existing.uploadedImageUrl,
                createdAt = existing.createdAt,
                modifiedAt = existing.modifiedAt,
                alreadyExists = true,
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

        userActivityRepository.findByUserIdAndPlaceId(userId, placeId)?.let { existing ->
            return UserActivityResponse(
                id = existing.id,
                userId = existing.userId,
                placeId = existing.placeId,
                missionId = existing.missionId,
                description = existing.description,
                isCorrect = existing.isCorrect,
                isCompleted = existing.isCompleted,
                pointsEarned = existing.pointsEarned,
                textAnswer = existing.textAnswer,
                selectedChoiceIndex = existing.selectedChoiceIndex,
                uploadedImageUrl = existing.uploadedImageUrl,
                createdAt = existing.createdAt,
                modifiedAt = existing.modifiedAt,
                alreadyExists = true,
            )
        }

        validateQuizAnswer(missionDetail, textAnswer, selectedChoiceIndex, uploadedImageUrl)
        val (isCorrect, aiResult) = checkAnswerWithAi(
            missionDetail,
            textAnswer,
            selectedChoiceIndex,
            uploadedImageUrl
        )

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
            modifiedAt = savedActivity.modifiedAt,
            aiResult = aiResult
        )
    }

    private fun validateQuizAnswer(
        missionDetail: MissionDetail,
        textAnswer: String?,
        selectedChoiceIndex: Int?,
        uploadedImageUrl: String?,
    ) {
        when (missionDetail.missionType) {
            QUIZ_MULTIPLE_CHOICE -> {
                if (selectedChoiceIndex == null) {
                    throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "선택지 인덱스")
                }
                if (missionDetail.choices == null) {
                    throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "선택지")
                }
                if (selectedChoiceIndex !in 0..(missionDetail.choices?.choices?.size?.minus(1) ?: 0)) {
                    throw InvalidRequestException(ErrorCode.INVALID_CHOICE_INDEX)
                }
            }

            QUIZ_TEXT_INPUT -> {
                if (textAnswer == null) {
                    throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "텍스트 답변")
                }
                if (textAnswer.isBlank()) {
                    throw InvalidRequestException(ErrorCode.TEXT_ANSWER_EMPTY)
                }
            }

            QUIZ_IMAGE_UPLOAD -> {
                if (uploadedImageUrl == null) {
                    throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "이미지 URL")
                }
            }

            PHOTO_UPLOAD -> {
                if (uploadedImageUrl == null) {
                    throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "이미지 URL")
                }
                imageUrlValidator.validateImageUrl(uploadedImageUrl)
            }

            SURVEY -> {
                if (textAnswer == null) {
                    throw InvalidRequestException(ErrorCode.REQUIRED_FIELD_MISSING, "설문 응답")
                }
                if (textAnswer.isBlank()) {
                    throw InvalidRequestException(ErrorCode.TEXT_ANSWER_EMPTY)
                }
            }

            PLACE_VISIT -> {
                // 별도 입력 필요 없음
            }

            else -> {
            }
        }
    }

    private fun checkAnswerWithAi(
        missionDetail: MissionDetail,
        textAnswer: String?,
        selectedChoiceIndex: Int?,
        uploadedImageUrl: String?,
    ): Pair<Boolean, AnswerCheckResult?> {
        return when (missionDetail.missionType) {
            QUIZ_MULTIPLE_CHOICE -> {
                val correctChoiceIndex = missionDetail.choices?.choices?.indexOf(missionDetail.answer)
                Pair(selectedChoiceIndex == correctChoiceIndex, null)
            }

            QUIZ_TEXT_INPUT -> {
                val correct = textAnswer?.trim()?.equals(missionDetail.answer?.trim(), ignoreCase = true) ?: false
                Pair(correct, null)
            }

            QUIZ_IMAGE_UPLOAD -> {
                val imageUrl = uploadedImageUrl ?: return Pair(false, null)
                val analysis = fallbackAIService.analyzeImage(imageUrl)
                val result: AnswerCheckResult = fallbackAIService.checkImageAnswer(missionDetail, analysis.extractedText)
                Pair(result.isCorrect && result.confidence >= 0.5, result.copy(extractedText = analysis.extractedText))
            }

            PHOTO_UPLOAD -> {
                val url = uploadedImageUrl ?: return Pair(false, null)
                imageUrlValidator.validateImageUrl(url)
                Pair(true, AnswerCheckResult(isCorrect = true, confidence = 1.0, reasoning = "이미지 업로드 완료"))
            }

            SURVEY -> {
                val ok = !(textAnswer.isNullOrBlank())
                Pair(ok, if (ok) AnswerCheckResult(true, 1.0, "설문 응답 접수") else null)
            }

            PLACE_VISIT -> {
                Pair(true, AnswerCheckResult(true, 1.0, "장소 방문 완료"))
            }

            else -> Pair(false, null)
        }
    }
} 
