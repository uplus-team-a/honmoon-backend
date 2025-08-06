package site.honmoon.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.user.dto.UserSummaryResponse
import site.honmoon.user.repository.UserSummaryRepository
import site.honmoon.point.repository.PointHistoryRepository
import site.honmoon.activity.repository.UserActivityRepository
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import java.util.*

/**
 * 사용자 요약, 포인트 현황, 퀴즈/미션 통계를 제공하는 서비스
 */
@Service
@Transactional(readOnly = true)
class UserSummaryService(
    private val userSummaryRepository: UserSummaryRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val userActivityRepository: UserActivityRepository
) {
    /**
     * 사용자 요약 정보를 조회한다.
     */
    fun getUserSummary(userId: UUID): UserSummaryResponse {
        val userSummary = userSummaryRepository.findByUserId(userId)
            ?: throw EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User ID: $userId")

        return UserSummaryResponse(
            id = userSummary.id,
            userId = userSummary.userId,
            totalPoints = userSummary.totalPoints,
            totalActivities = userSummary.totalActivities,
            createdAt = userSummary.createdAt,
            modifiedAt = userSummary.modifiedAt
        )
    }

    /**
     * 사용자 포인트 현황(현재/누적획득/누적사용)을 조회한다.
     */
    fun getUserPoints(userId: UUID): Map<String, Int> {
        val userSummary = userSummaryRepository.findByUserId(userId)
            ?: throw EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User ID: $userId")

        val histories = pointHistoryRepository.findByUserId(userId)
        val totalEarned = histories.filter { it.points > 0 }.sumOf { it.points }
        val totalUsed = histories.filter { it.points < 0 }.sumOf { -it.points }

        return mapOf(
            "currentPoints" to userSummary.totalPoints,
            "totalEarned" to totalEarned,
            "totalUsed" to totalUsed
        )
    }

    /**
     * 사용자 퀴즈 활동 통계를 조회한다.
     */
    fun getUserQuizStats(userId: UUID): Map<String, Any> {
        val quizActivities = userActivityRepository.findByUserId(userId).filter { it.isCorrect != null }
        val totalQuizzes = quizActivities.size
        val correctQuizzes = quizActivities.count { it.isCorrect == true }
        val accuracy = if (totalQuizzes > 0) (correctQuizzes.toDouble() / totalQuizzes * 100).toInt() else 0

        return mapOf(
            "totalQuizzes" to totalQuizzes,
            "correctQuizzes" to correctQuizzes,
            "accuracy" to accuracy,
            "totalPointsEarned" to quizActivities.sumOf { it.pointsEarned }
        )
    }

    /**
     * 사용자 미션 통계를 조회한다.
     */
    fun getUserMissionStats(userId: UUID): Map<String, Any> {
        val missionActivities = userActivityRepository.findByUserId(userId)
        val totalMissions = missionActivities.size
        val completedMissions = missionActivities.count { it.isCompleted }
        val completionRate = if (totalMissions > 0) (completedMissions.toDouble() / totalMissions * 100).toInt() else 0

        return mapOf(
            "totalMissions" to totalMissions,
            "completedMissions" to completedMissions,
            "completionRate" to completionRate,
            "totalPointsEarned" to missionActivities.sumOf { it.pointsEarned }
        )
    }
} 