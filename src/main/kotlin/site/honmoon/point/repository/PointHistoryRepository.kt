package site.honmoon.point.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.point.entity.PointHistory
import java.util.*

@Repository
interface PointHistoryRepository : JpaRepository<PointHistory, Long> {
    fun findByUserId(userId: UUID): List<PointHistory>
    fun findByUserIdAndDescription(userId: UUID, description: String): List<PointHistory>
} 