package site.honmoon.mission.service

import site.honmoon.mission.entity.MissionDetail

interface AnswerChecker {
    fun checkAnswer(mission: MissionDetail, userAnswer: String): Boolean
    fun supports(missionType: site.honmoon.mission.type.MissionType): Boolean
}