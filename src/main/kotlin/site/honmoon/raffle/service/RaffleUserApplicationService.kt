package site.honmoon.raffle.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.DuplicateResourceException
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.point.service.PointHistoryService
import site.honmoon.raffle.dto.RaffleUserApplicationResponse
import site.honmoon.raffle.entity.RaffleUserApplication
import site.honmoon.raffle.repository.RaffleProductRepository
import site.honmoon.raffle.repository.RaffleUserApplicationRepository
import java.time.LocalDateTime
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

        raffleUserApplicationRepository.findByUserIdAndRaffleProductId(userId, raffleProductId)?.let {
            throw DuplicateResourceException(ErrorCode.DUPLICATE_ACTIVITY, "이미 해당 래플에 응모했습니다.")
        }

        pointHistoryService.usePointsForRaffle(userId, raffleProductId)

        val raffleApplication = RaffleUserApplication(
            userId = userId,
            raffleProductId = raffleProductId,
            applicationDate = LocalDateTime.now()
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
