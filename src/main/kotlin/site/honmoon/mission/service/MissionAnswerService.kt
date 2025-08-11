package site.honmoon.mission.service

import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.mission.dto.AnswerCheckResult
import site.honmoon.mission.dto.MissionAnswerResponse
import site.honmoon.mission.repository.MissionDetailRepository
import site.honmoon.mission.type.MissionType
import site.honmoon.point.service.PointHistoryService
import java.util.*

@Service
class MissionAnswerService(
    private val missionDetailRepository: MissionDetailRepository,
    private val answerCheckers: List<AnswerChecker>,
    private val fallbackAIService: FallbackAIService,
    private val pointHistoryService: PointHistoryService,
) {

    fun checkAnswer(missionId: Long, userAnswer: String): AnswerCheckResult {
        val mission = missionDetailRepository.findByIdOrNull(missionId)
            ?: throw EntityNotFoundException(ErrorCode.MISSION_NOT_FOUND, "ID: $missionId")

        val checker = answerCheckers.find { it.supports(mission.missionType) }

        return if (checker != null) {
            val isCorrect = checker.checkAnswer(mission, userAnswer)
            AnswerCheckResult(
                isCorrect = isCorrect,
                confidence = 1.0,
                reasoning = if (isCorrect) "정확한 답변입니다." else "답변이 일치하지 않습니다.",
                hint = if (isCorrect) "" else "힌트를 확인해보세요"
            )
        } else {
            fallbackAIService.checkTextAnswer(mission, userAnswer)
        }
    }

    fun checkAnswerWithImage(missionId: Long, imageUrl: String): AnswerCheckResult {
        val mission = missionDetailRepository.findByIdOrNull(missionId)
            ?: throw EntityNotFoundException(ErrorCode.MISSION_NOT_FOUND, "ID: $missionId")

        if (mission.missionType != MissionType.QUIZ_IMAGE_UPLOAD) {
            throw IllegalArgumentException("This mission type doesn't support image upload")
        }

        val imageAnalysis = fallbackAIService.analyzeImage(imageUrl)
        val answerCheck = fallbackAIService.checkImageAnswer(mission, imageAnalysis.extractedText)

        return answerCheck.copy(extractedText = imageAnalysis.extractedText)
    }

    fun submitAnswer(missionId: Long, userAnswer: String, userId: UUID): MissionAnswerResponse {
        val mission = missionDetailRepository.findByIdOrNull(missionId)
            ?: throw EntityNotFoundException(ErrorCode.MISSION_NOT_FOUND, "ID: $missionId")

        val checkResult = checkAnswer(missionId, userAnswer)

        val pointsEarned = if (checkResult.isCorrect && checkResult.confidence >= 0.5) {
            pointHistoryService.earnPointsFromQuiz(userId, missionId, mission.points)
            mission.points
        } else {
            0
        }

        return MissionAnswerResponse(
            isCorrect = checkResult.isCorrect,
            pointsEarned = pointsEarned,
            explanation = if (checkResult.isCorrect) {
                mission.answerExplanation ?: checkResult.reasoning
            } else {
                checkResult.reasoning
            },
            hint = if (!checkResult.isCorrect) checkResult.hint?.ifBlank { null } else null
        )
    }

    fun submitAnswerWithImage(missionId: Long, imageUrl: String, userId: UUID): MissionAnswerResponse {
        val mission = missionDetailRepository.findByIdOrNull(missionId)
            ?: throw EntityNotFoundException(ErrorCode.MISSION_NOT_FOUND, "ID: $missionId")

        val checkResult = checkAnswerWithImage(missionId, imageUrl)

        val pointsEarned = if (checkResult.isCorrect && checkResult.confidence >= 0.5) {
            pointHistoryService.earnPointsFromQuiz(userId, missionId, mission.points)
            mission.points
        } else {
            0
        }

        return MissionAnswerResponse(
            isCorrect = checkResult.isCorrect,
            pointsEarned = pointsEarned,
            explanation = if (checkResult.isCorrect) {
                mission.answerExplanation ?: checkResult.reasoning
            } else {
                "추출된 텍스트: '${checkResult.extractedText}' - ${checkResult.reasoning}"
            },
            hint = if (!checkResult.isCorrect) checkResult.hint?.ifBlank { null } else null
        )
    }
}
