package site.honmoon.mission.service

import org.springframework.stereotype.Component
import site.honmoon.mission.entity.MissionDetail
import site.honmoon.mission.type.MissionType

@Component
class SimpleAnswerChecker : AnswerChecker {

    override fun checkAnswer(mission: MissionDetail, userAnswer: String): Boolean {
        return when (mission.missionType) {
            MissionType.QUIZ_MULTIPLE_CHOICE -> checkMultipleChoiceAnswer(mission, userAnswer)
            MissionType.QUIZ_TEXT_INPUT -> checkTextInputAnswer(mission, userAnswer)
            else -> false
        }
    }

    override fun supports(missionType: MissionType): Boolean {
        return missionType in listOf(
            MissionType.QUIZ_MULTIPLE_CHOICE,
            MissionType.QUIZ_TEXT_INPUT
        )
    }

    private fun checkMultipleChoiceAnswer(mission: MissionDetail, userAnswer: String): Boolean {
        return mission.answer?.equals(userAnswer, ignoreCase = true) == true
    }

    private fun checkTextInputAnswer(mission: MissionDetail, userAnswer: String): Boolean {
        return mission.answer?.equals(userAnswer.trim(), ignoreCase = true) == true
    }
}
