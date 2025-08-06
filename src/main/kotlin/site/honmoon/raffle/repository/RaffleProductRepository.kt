package site.honmoon.raffle.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.raffle.entity.RaffleProduct

@Repository
interface RaffleProductRepository : JpaRepository<RaffleProduct, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<RaffleProduct>
    fun findByPointCostBetween(minPoints: Int, maxPoints: Int): List<RaffleProduct>
} 