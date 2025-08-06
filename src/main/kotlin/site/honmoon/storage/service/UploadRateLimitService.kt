package site.honmoon.storage.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class UploadRateLimitService {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_UPLOADS_PER_HOUR = 50
        private const val MAX_UPLOADS_PER_DAY = 200
        private const val WINDOW_SIZE_HOURS = 1
        private const val WINDOW_SIZE_DAYS = 24
    }

    private val hourlyUploads = ConcurrentHashMap<String, MutableList<LocalDateTime>>()
    private val dailyUploads = ConcurrentHashMap<String, MutableList<LocalDateTime>>()
    private val uploadCounters = ConcurrentHashMap<String, AtomicInteger>()

    fun canUpload(userId: String): Boolean {
        val now = LocalDateTime.now()

        val hourlyCount = getHourlyUploadCount(userId, now)
        if (hourlyCount >= MAX_UPLOADS_PER_HOUR) {
            logger.warn { "사용자 ${'$'}userId의 시간별 업로드 제한 초과: ${'$'}hourlyCount/${'$'}MAX_UPLOADS_PER_HOUR" }
            return false
        }

        val dailyCount = getDailyUploadCount(userId, now)
        if (dailyCount >= MAX_UPLOADS_PER_DAY) {
            logger.warn { "사용자 ${'$'}userId의 일별 업로드 제한 초과: ${'$'}dailyCount/${'$'}MAX_UPLOADS_PER_DAY" }
            return false
        }

        return true
    }

    fun recordUpload(userId: String) {
        val now = LocalDateTime.now()
        hourlyUploads.computeIfAbsent(userId) { mutableListOf() }.add(now)
        dailyUploads.computeIfAbsent(userId) { mutableListOf() }.add(now)
        uploadCounters.computeIfAbsent(userId) { AtomicInteger(0) }.incrementAndGet()
        logger.info { "사용자 ${'$'}userId 업로드 기록됨" }
    }

    fun getUploadStats(userId: String): UploadStatsDto {
        val now = LocalDateTime.now()
        val hourlyCount = getHourlyUploadCount(userId, now)
        val dailyCount = getDailyUploadCount(userId, now)
        val totalCount = uploadCounters[userId]?.get() ?: 0

        return UploadStatsDto(
            userId = userId,
            hourlyUploads = hourlyCount,
            dailyUploads = dailyCount,
            totalUploads = totalCount,
            hourlyLimit = MAX_UPLOADS_PER_HOUR,
            dailyLimit = MAX_UPLOADS_PER_DAY,
            canUpload = canUpload(userId),
            checkedAt = now
        )
    }

    private fun getHourlyUploadCount(userId: String, now: LocalDateTime): Int {
        val userHourlyUploads = hourlyUploads[userId] ?: return 0
        val cutoffTime = now.minusHours(WINDOW_SIZE_HOURS.toLong())
        userHourlyUploads.removeAll { it.isBefore(cutoffTime) }
        return userHourlyUploads.size
    }

    private fun getDailyUploadCount(userId: String, now: LocalDateTime): Int {
        val userDailyUploads = dailyUploads[userId] ?: return 0
        val cutoffTime = now.minusHours(WINDOW_SIZE_DAYS.toLong())
        userDailyUploads.removeAll { it.isBefore(cutoffTime) }
        return userDailyUploads.size
    }

    fun resetUserUploads(userId: String) {
        hourlyUploads.remove(userId)
        dailyUploads.remove(userId)
        uploadCounters.remove(userId)
        logger.info { "사용자 ${'$'}userId의 업로드 기록이 초기화되었습니다." }
    }
}

data class UploadStatsDto(
    val userId: String,
    val hourlyUploads: Int,
    val dailyUploads: Int,
    val totalUploads: Int,
    val hourlyLimit: Int,
    val dailyLimit: Int,
    val canUpload: Boolean,
    val checkedAt: LocalDateTime
) 
