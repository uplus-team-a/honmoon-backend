package site.honmoon.mission.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import site.honmoon.mission.entity.MissionPlace

@Repository
interface MissionPlaceRepository : JpaRepository<MissionPlace, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<MissionPlace>

    @Query(
        value = """
            SELECT *
            FROM mission_place mp
            WHERE mp.latitude IS NOT NULL AND mp.longitude IS NOT NULL
            ORDER BY (
              (mp.latitude - :lat) * (mp.latitude - :lat) +
              (mp.longitude - :lng) * (mp.longitude - :lng)
            ) ASC
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findNearest(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("limit") limit: Int
    ): List<MissionPlace>

    @Query(
        value = """
            SELECT *
            FROM mission_place mp
            WHERE mp.location_vec IS NOT NULL
            ORDER BY mp.location_vec <-> ARRAY[CAST(:lat AS float4), CAST(:lng AS float4)]::vector
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findNearestByVector(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("limit") limit: Int
    ): List<MissionPlace>
} 