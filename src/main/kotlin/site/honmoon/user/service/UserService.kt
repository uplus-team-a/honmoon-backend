package site.honmoon.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.activity.repository.UserActivityRepository
import site.honmoon.common.Constant
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.point.entity.PointHistory
import site.honmoon.common.exception.InvalidRequestException
import site.honmoon.point.repository.PointHistoryRepository
import site.honmoon.user.dto.UserResponse
import site.honmoon.user.entity.Users
import site.honmoon.user.repository.UsersRepository
import java.util.*

@Service
@Transactional(readOnly = true)
class UserService(
    private val usersRepository: UsersRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val userActivityRepository: UserActivityRepository,
) {
    /**
     * 사용자 ID로 사용자 조회. 없으면 예외 발생.
     */
    fun getByIdOrThrow(userId: UUID): Users {
        return usersRepository.findById(userId).orElseThrow {
            EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User ID: $userId")
        }
    }

    /**
     * 이메일 기준 오름차순 첫 번째 사용자 반환. 없으면 예외.
     */
    fun getFirstUserByEmailAscOrThrow(): Users {
        return usersRepository.findFirstByEmailIsNotNullOrderByEmailAsc()
            ?: throw EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "No users found (email asc)")
    }

    /**
     * 사용자 ID로 이메일 조회. 비어있거나 없으면 예외.
     */
    fun getEmailByUserId(userId: UUID): String {
        val user = getByIdOrThrow(userId)
        val email = user.email?.trim()
        if (email.isNullOrBlank()) {
            throw InvalidRequestException(ErrorCode.USER_EMAIL_NOT_FOUND)
        }
        return email
    }

    /**
     * 사용자 정보를 조회한다.
     */
    fun getUser(userId: UUID): UserResponse {
        val user = getByIdOrThrow(userId)
        return UserResponse(
            id = user.id,
            email = user.email,
            nickname = user.nickname,
            totalPoints = user.totalPoints,
            totalActivities = user.totalActivities,
            profileImageUrl = user.profileImageUrl,
            isActive = user.isActive ?: true,
            createdAt = user.createdAt,
            modifiedAt = user.modifiedAt
        )
    }

    /**
     * 사용자 포인트 현황을 조회한다.
     */
    fun getUserPoints(userId: UUID): Map<String, Int> {
        val user = getByIdOrThrow(userId)
        val histories = pointHistoryRepository.findByUserId(userId)
        val totalEarned = histories.filter { it.points > 0 }.sumOf { it.points }
        val totalUsed = histories.filter { it.points < 0 }.sumOf { -it.points }

        return mapOf(
            "currentPoints" to user.totalPoints,
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

    /**
     * Google OAuth 로그인 시 사용자 정보를 기반으로 회원을 조회하거나 새로 생성한다.
     */
    @Transactional
    fun getOrCreateUserFromGoogle(
        googleSub: String,
        email: String?,
        name: String?,
        pictureUrl: String?,
    ): Users {
        val existing = email?.takeIf { it.isNotBlank() }?.let { usersRepository.findByEmail(it) }
        if (existing != null) {
            val nickname = name?.takeIf { it.isNotBlank() }
            if (nickname != null && existing.nickname != nickname) {
                existing.nickname = nickname
            }
            return usersRepository.save(existing)
        }

        val welcome = Constant.WELCOME_POINTS_DEFAULT
        val newUser = Users(
            id = UUID.randomUUID(),
            email = email,
            nickname = name,
            totalPoints = welcome,
            totalActivities = 0,
            profileImageUrl = pictureUrl,
            isActive = true,
        )
        val savedUser = usersRepository.save(newUser)

        if (welcome > 0) {
            val ph = PointHistory(
                userId = savedUser.id,
                points = welcome,
                description = "웰컴 포인트 지급",
            )
            pointHistoryRepository.save(ph)
        }
        
        return savedUser
    }

    /**
     * 프로필 이미지를 업데이트한다.
     */
    @Transactional
    fun updateProfileImage(userId: UUID, imageUrl: String): UserResponse {
        val user = getByIdOrThrow(userId)
        user.profileImageUrl = imageUrl
        usersRepository.save(user)
        return getUser(userId)
    }

    /**
     * 프로필 정보를 업데이트한다.
     */
    @Transactional
    fun updateProfile(userId: UUID, nickname: String?, profileImageUrl: String?): UserResponse {
        val user = getByIdOrThrow(userId)
        nickname?.let { user.nickname = it }
        profileImageUrl?.let { user.profileImageUrl = it }
        usersRepository.save(user)
        return getUser(userId)
    }
}


