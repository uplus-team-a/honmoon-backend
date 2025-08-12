package site.honmoon.raffle.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.DuplicateResourceException
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.point.service.PointHistoryService
import site.honmoon.raffle.dto.RaffleUserApplicationResponse
import site.honmoon.raffle.dto.RaffleApplyResult
import site.honmoon.raffle.entity.RaffleUserApplication
import site.honmoon.raffle.repository.RaffleProductRepository
import site.honmoon.raffle.repository.RaffleUserApplicationRepository
import java.time.OffsetDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class RaffleUserApplicationService(
    private val raffleUserApplicationRepository: RaffleUserApplicationRepository,
    private val raffleProductRepository: RaffleProductRepository,
    private val pointHistoryService: PointHistoryService,
) {
    fun getRaffleUserApplication(id: Long): RaffleUserApplicationResponse {
        val raffleUserApplication = raffleUserApplicationRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException(ErrorCode.RAFFLE_NOT_FOUND, "ID: $id")

        return RaffleUserApplicationResponse(
            id = raffleUserApplication.id,
            userId = raffleUserApplication.userId,
            raffleProductId = raffleUserApplication.raffleProductId,
            applicationDate = raffleUserApplication.applicationDate,
            createdAt = raffleUserApplication.createdAt,
            modifiedAt = raffleUserApplication.modifiedAt
        )
    }

    fun getUserRaffleApplications(userId: UUID): List<RaffleUserApplicationResponse> {
        return raffleUserApplicationRepository.findByUserId(userId).map { application ->
            RaffleUserApplicationResponse(
                id = application.id,
                userId = application.userId,
                raffleProductId = application.raffleProductId,
                applicationDate = application.applicationDate,
                createdAt = application.createdAt,
                modifiedAt = application.modifiedAt
            )
        }
    }

    fun getRaffleApplicationsByProduct(productId: Long): List<RaffleUserApplicationResponse> {
        return raffleUserApplicationRepository.findByRaffleProductId(productId).map { application ->
            RaffleUserApplicationResponse(
                id = application.id,
                userId = application.userId,
                raffleProductId = application.raffleProductId,
                applicationDate = application.applicationDate,
                createdAt = application.createdAt,
                modifiedAt = application.modifiedAt
            )
        }
    }

    @Transactional
    fun applyRaffle(userId: UUID, raffleProductId: Long): RaffleUserApplicationResponse {
        raffleProductRepository.findByIdOrNull(raffleProductId)
            ?: throw EntityNotFoundException(ErrorCode.RAFFLE_NOT_FOUND, "ID: $raffleProductId")

        raffleUserApplicationRepository.findByUserIdAndRaffleProductId(userId, raffleProductId)?.let { existing ->
            return RaffleUserApplicationResponse(
                id = existing.id,
                userId = existing.userId,
                raffleProductId = existing.raffleProductId,
                applicationDate = existing.applicationDate,
                createdAt = existing.createdAt,
                modifiedAt = existing.modifiedAt
            )
        }

        pointHistoryService.usePointsForRaffle(userId, raffleProductId)

        val raffleApplication = RaffleUserApplication(
            userId = userId,
            raffleProductId = raffleProductId,
            applicationDate = OffsetDateTime.now()
        )

        val savedApplication = raffleUserApplicationRepository.save(raffleApplication)

        return RaffleUserApplicationResponse(
            id = savedApplication.id,
            userId = savedApplication.userId,
            raffleProductId = savedApplication.raffleProductId,
            applicationDate = savedApplication.applicationDate,
            createdAt = savedApplication.createdAt,
            modifiedAt = savedApplication.modifiedAt
        )
    }

    /**
     * 포인트 부족/중복 등의 사유를 200 응답으로 전달하기 위한 상태형 응모 메서드
     */
    @Transactional
    fun applyRaffleWithStatus(userId: UUID, raffleProductId: Long): RaffleApplyResult {
        val product = raffleProductRepository.findByIdOrNull(raffleProductId)
            ?: return RaffleApplyResult(
                success = false,
                reasonCode = ErrorCode.RAFFLE_NOT_FOUND.name,
            )

        raffleUserApplicationRepository.findByUserIdAndRaffleProductId(userId, raffleProductId)?.let { existing ->
            return RaffleApplyResult(
                success = false,
                reasonCode = ErrorCode.DUPLICATE_ACTIVITY.name,
                application = RaffleUserApplicationResponse(
                    id = existing.id,
                    userId = existing.userId,
                    raffleProductId = existing.raffleProductId,
                    applicationDate = existing.applicationDate,
                    createdAt = existing.createdAt,
                    modifiedAt = existing.modifiedAt,
                )
            )
        }

        val userPoints = pointHistoryService.tryUsePoints(userId, product.pointCost)
        if (!userPoints.success) {
            return RaffleApplyResult(
                success = false,
                reasonCode = userPoints.reasonCode,
                requiredPoints = userPoints.requiredPoints,
                currentPoints = userPoints.currentPoints,
            )
        }

        pointHistoryService.usePointsForRaffle(userId, raffleProductId)

        val raffleApplication = RaffleUserApplication(
            userId = userId,
            raffleProductId = raffleProductId,
            applicationDate = OffsetDateTime.now()
        )
        val saved = raffleUserApplicationRepository.save(raffleApplication)

        return RaffleApplyResult(
            success = true,
            application = RaffleUserApplicationResponse(
                id = saved.id,
                userId = saved.userId,
                raffleProductId = saved.raffleProductId,
                applicationDate = saved.applicationDate,
                createdAt = saved.createdAt,
                modifiedAt = saved.modifiedAt,
            )
        )
    }

    fun drawRaffleWinners(productId: Long, winnerCount: Int): List<RaffleUserApplicationResponse> {
        raffleProductRepository.findByIdOrNull(productId)
            ?: throw EntityNotFoundException(ErrorCode.RAFFLE_NOT_FOUND, "ID: $productId")
        val applications = raffleUserApplicationRepository.findByRaffleProductId(productId)
        val winners = applications.shuffled().take(winnerCount)

        return winners.map { application ->
            RaffleUserApplicationResponse(
                id = application.id,
                userId = application.userId,
                raffleProductId = application.raffleProductId,
                applicationDate = application.applicationDate,
                createdAt = application.createdAt,
                modifiedAt = application.modifiedAt
            )
        }
    }

    fun getUserApplicationStatus(userId: UUID, productId: Long): RaffleUserApplicationResponse? {
        val application = raffleUserApplicationRepository.findByUserIdAndRaffleProductId(userId, productId)

        return application?.let {
            RaffleUserApplicationResponse(
                id = it.id,
                userId = it.userId,
                raffleProductId = it.raffleProductId,
                applicationDate = it.applicationDate,
                createdAt = it.createdAt,
                modifiedAt = it.modifiedAt
            )
        }
    }
} 
