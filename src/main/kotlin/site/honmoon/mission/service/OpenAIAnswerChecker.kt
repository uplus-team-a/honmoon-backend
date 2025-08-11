package site.honmoon.mission.service

import org.springframework.stereotype.Component
import site.honmoon.mission.entity.MissionDetail
import site.honmoon.mission.type.MissionType

@Component
class OpenAIAnswerChecker(
    private val fallbackAIService: FallbackAIService,
) : AnswerChecker {

    override fun checkAnswer(mission: MissionDetail, userAnswer: String): Boolean {
        val result = fallbackAIService.checkTextAnswer(mission, userAnswer)
        return result.isCorrect && result.confidence >= 0.5
    }

    override fun supports(missionType: MissionType): Boolean {
        return missionType == MissionType.QUIZ_IMAGE_UPLOAD
    }
}
