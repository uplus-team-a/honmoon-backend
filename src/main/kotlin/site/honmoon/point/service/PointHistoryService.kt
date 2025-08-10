package site.honmoon.point.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.point.dto.PointHistoryResponse
import site.honmoon.point.entity.PointHistory
import site.honmoon.point.repository.PointHistoryRepository
import site.honmoon.user.repository.UserSummaryRepository
import site.honmoon.raffle.repository.RaffleProductRepository
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import java.util.*

/**
 * 포인트 내역 조회, 적립, 차감을 담당하는 서비스
 */
@Service
@Transactional(readOnly = true)
class PointHistoryService(
    private val pointHistoryRepository: PointHistoryRepository,
    private val userSummaryRepository: UserSummaryRepository,
    private val raffleProductRepository: RaffleProductRepository,
) {
    /**
     * 포인트 내역 단건을 조회한다.
     */
    fun getPointHistory(id: Long): PointHistoryResponse {
        val pointHistory = pointHistoryRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException(ErrorCode.POINT_HISTORY_NOT_FOUND, "ID: $id")

        return PointHistoryResponse(
            id = pointHistory.id,
            userId = pointHistory.userId,
            points = pointHistory.points,
            description = pointHistory.description,
            createdAt = pointHistory.createdAt,
            modifiedAt = pointHistory.modifiedAt
        )
    }

    /**
     * 사용자의 전체 포인트 내역을 조회한다.
     */
    fun getUserPointHistory(userId: UUID): List<PointHistoryResponse> {
        return pointHistoryRepository.findByUserId(userId).map { pointHistory ->
            PointHistoryResponse(
                id = pointHistory.id,
                userId = pointHistory.userId,
                points = pointHistory.points,
                description = pointHistory.description,
                createdAt = pointHistory.createdAt,
                modifiedAt = pointHistory.modifiedAt
            )
        }
    }

    /**
     * 사용자의 포인트 획득 내역만 조회한다.
     */
    fun getUserEarnedPointHistory(userId: UUID): List<PointHistoryResponse> {
        return pointHistoryRepository.findByUserId(userId)
            .filter { it.points > 0 }
            .map { pointHistory ->
                PointHistoryResponse(
                    id = pointHistory.id,
                    userId = pointHistory.userId,
                    points = pointHistory.points,
                    description = pointHistory.description,
                    createdAt = pointHistory.createdAt,
                    modifiedAt = pointHistory.modifiedAt
                )
            }
    }

    /**
     * 사용자의 포인트 사용 내역만 조회한다.
     */
    fun getUserUsedPointHistory(userId: UUID): List<PointHistoryResponse> {
        return pointHistoryRepository.findByUserId(userId)
            .filter { it.points < 0 }
            .map { pointHistory ->
                PointHistoryResponse(
                    id = pointHistory.id,
                    userId = pointHistory.userId,
                    points = pointHistory.points,
                    description = pointHistory.description,
                    createdAt = pointHistory.createdAt,
                    modifiedAt = pointHistory.modifiedAt
                )
            }
    }

    /**
     * 퀴즈 정답 보상으로 포인트를 적립한다.
     */
    @Transactional
    fun earnPointsFromQuiz(userId: UUID, quizId: Long, points: Int): PointHistoryResponse {
        val pointHistory = PointHistory(
            userId = userId,
            points = points,
            description = "퀴즈 완료 보상 (퀴즈 ID: $quizId)"
        )

        val savedPointHistory = pointHistoryRepository.save(pointHistory)

        val userSummary = userSummaryRepository.findByUserId(userId)
        userSummary?.let {
            it.totalPoints += points
            userSummaryRepository.save(it)
        }

        return PointHistoryResponse(
            id = savedPointHistory.id,
            userId = savedPointHistory.userId,
            points = savedPointHistory.points,
            description = savedPointHistory.description,
            createdAt = savedPointHistory.createdAt,
            modifiedAt = savedPointHistory.modifiedAt
        )
    }

    /**
     * 래플 응모를 위해 상품의 pointCost만큼 포인트를 차감한다.
     */
    @Transactional
    fun usePointsForRaffle(userId: UUID, raffleProductId: Long): PointHistoryResponse {
        val raffleProduct = raffleProductRepository.findByIdOrNull(raffleProductId)
            ?: throw EntityNotFoundException(ErrorCode.RAFFLE_NOT_FOUND, "ID: $raffleProductId")
        val requiredPoints = raffleProduct.pointCost

        val userSummary = userSummaryRepository.findByUserId(userId)
            ?: throw EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User ID: $userId")

        if (userSummary.totalPoints < requiredPoints) {
            throw IllegalArgumentException("포인트가 부족합니다. 필요: $requiredPoints, 보유: ${userSummary.totalPoints}")
        }

        val pointHistory = PointHistory(
            userId = userId,
            points = -requiredPoints,
            description = "래플 응모 (상품 ID: $raffleProductId)"
        )

        val savedPointHistory = pointHistoryRepository.save(pointHistory)

        userSummary.totalPoints -= requiredPoints
        userSummaryRepository.save(userSummary)

        return PointHistoryResponse(
            id = savedPointHistory.id,
            userId = savedPointHistory.userId,
            points = savedPointHistory.points,
            description = savedPointHistory.description,
            createdAt = savedPointHistory.createdAt,
            modifiedAt = savedPointHistory.modifiedAt
        )
    }
} 
