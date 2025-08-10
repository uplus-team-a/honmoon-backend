package site.honmoon.raffle.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.raffle.dto.RaffleProductResponse
import site.honmoon.raffle.entity.RaffleProduct
import site.honmoon.raffle.repository.RaffleProductRepository
import site.honmoon.raffle.repository.RaffleUserApplicationRepository
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException

@Service
@Transactional(readOnly = true)
class RaffleProductService(
    private val raffleProductRepository: RaffleProductRepository,
    private val raffleUserApplicationRepository: RaffleUserApplicationRepository
) {
    fun getRaffleProduct(id: Long): RaffleProductResponse {
        val raffleProduct = raffleProductRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException(ErrorCode.RAFFLE_NOT_FOUND, "ID: $id")
        
        return RaffleProductResponse(
            id = raffleProduct.id,
            name = raffleProduct.name,
            description = raffleProduct.description,
            imageUrl = raffleProduct.imageUrl,
            createdAt = raffleProduct.createdAt,
            modifiedAt = raffleProduct.modifiedAt
        )
    }
    
    fun getRaffleProducts(): List<RaffleProductResponse> {
        return raffleProductRepository.findAll().map { raffleProduct ->
            RaffleProductResponse(
                id = raffleProduct.id,
                name = raffleProduct.name,
                description = raffleProduct.description,
                imageUrl = raffleProduct.imageUrl,
                createdAt = raffleProduct.createdAt,
                modifiedAt = raffleProduct.modifiedAt
            )
        }
    }
    
    fun searchRaffleProducts(name: String): List<RaffleProductResponse> {
        return raffleProductRepository.findByNameContainingIgnoreCase(name).map { raffleProduct ->
            RaffleProductResponse(
                id = raffleProduct.id,
                name = raffleProduct.name,
                description = raffleProduct.description,
                imageUrl = raffleProduct.imageUrl,
                createdAt = raffleProduct.createdAt,
                modifiedAt = raffleProduct.modifiedAt
            )
        }
    }
    
    fun getRaffleProductsByPoints(minPoints: Int, maxPoints: Int): List<RaffleProductResponse> {
        val products = raffleProductRepository.findByPointCostBetween(minPoints, maxPoints)
        return products.map { raffleProduct ->
            RaffleProductResponse(
                id = raffleProduct.id,
                name = raffleProduct.name,
                description = raffleProduct.description,
                imageUrl = raffleProduct.imageUrl,
                createdAt = raffleProduct.createdAt,
                modifiedAt = raffleProduct.modifiedAt
            )
        }
    }
    
    fun getApplicantsCount(id: Long): Map<String, Int> {
        val applications = raffleUserApplicationRepository.findByRaffleProductId(id)
        return mapOf("applicantsCount" to applications.size)
    }
} 
