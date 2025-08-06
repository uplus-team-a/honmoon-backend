package site.honmoon.raffle.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.raffle.entity.RaffleUserApplication
import java.util.*

@Repository
interface RaffleUserApplicationRepository : JpaRepository<RaffleUserApplication, Long> {
    fun findByUserId(userId: UUID): List<RaffleUserApplication>
    fun findByRaffleProductId(raffleProductId: Long): List<RaffleUserApplication>
    fun findByUserIdAndRaffleProductId(userId: UUID, raffleProductId: Long): RaffleUserApplication?
} 