package site.honmoon.activity.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.activity.entity.UserActivity
import java.util.*

@Repository
interface UserActivityRepository : JpaRepository<UserActivity, Long> {
    fun findByUserId(userId: UUID): List<UserActivity>
    fun findByPlaceId(placeId: Long): List<UserActivity>
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<UserActivity>
    fun findByUserIdAndPlaceId(userId: UUID, placeId: Long): UserActivity?
    fun findByMissionId(missionId: Long): List<UserActivity>
} 