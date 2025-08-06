package site.honmoon.mission.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.mission.entity.MissionDetail

@Repository
interface MissionDetailRepository : JpaRepository<MissionDetail, Long> {
    fun findByPlaceId(placeId: Long): List<MissionDetail>
} 