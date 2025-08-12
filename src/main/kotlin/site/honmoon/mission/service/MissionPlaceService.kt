package site.honmoon.mission.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.common.ErrorCode
import site.honmoon.common.exception.EntityNotFoundException
import site.honmoon.mission.dto.MissionPlaceResponse
import site.honmoon.mission.repository.MissionDetailRepository
import site.honmoon.mission.repository.MissionPlaceRepository

@Service
@Transactional(readOnly = true)
class MissionPlaceService(
    private val missionPlaceRepository: MissionPlaceRepository,
    private val missionDetailRepository: MissionDetailRepository,
) {
    fun getMissionPlace(id: Long): MissionPlaceResponse {
        val missionPlace = missionPlaceRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException(ErrorCode.PLACE_NOT_FOUND, "ID: $id")

        return MissionPlaceResponse(
            id = missionPlace.id,
            name = missionPlace.name,
            description = missionPlace.description,
            location = missionPlace.location,
            image = missionPlace.image,
            latitude = missionPlace.latitude,
            longitude = missionPlace.longitude,
            createdAt = missionPlace.createdAt,
            modifiedAt = missionPlace.modifiedAt
        )
    }

    fun getMissionPlaces(): List<MissionPlaceResponse> {
        return missionPlaceRepository.findAll().map { missionPlace ->
            MissionPlaceResponse(
                id = missionPlace.id,
                name = missionPlace.name,
                description = missionPlace.description,
                location = missionPlace.location,
                image = missionPlace.image,
                latitude = missionPlace.latitude,
                longitude = missionPlace.longitude,
                createdAt = missionPlace.createdAt,
                modifiedAt = missionPlace.modifiedAt
            )
        }
    }

    fun searchMissionPlaces(title: String): List<MissionPlaceResponse> {
        return missionPlaceRepository.findByNameContainingIgnoreCase(title).map { missionPlace ->
            MissionPlaceResponse(
                id = missionPlace.id,
                name = missionPlace.name,
                description = missionPlace.description,
                location = missionPlace.location,
                image = missionPlace.image,
                latitude = missionPlace.latitude,
                longitude = missionPlace.longitude,
                createdAt = missionPlace.createdAt,
                modifiedAt = missionPlace.modifiedAt
            )
        }
    }

    fun getNearbyMissionPlaces(lat: Double, lng: Double, radius: Int): List<MissionPlaceResponse> {
        val limit = if (radius <= 0) 20 else minOf(radius / 50, 100).coerceAtLeast(1)
        val vectorNearest = try {
            missionPlaceRepository.findNearestByVector(lat, lng, limit)
        } catch (_: Exception) {
            emptyList()
        }
        val places =
            if (vectorNearest.isNotEmpty()) vectorNearest else missionPlaceRepository.findNearest(lat, lng, limit)
        return places.map { missionPlace ->
            MissionPlaceResponse(
                id = missionPlace.id,
                name = missionPlace.name,
                description = missionPlace.description,
                location = missionPlace.location,
                image = missionPlace.image,
                latitude = missionPlace.latitude,
                longitude = missionPlace.longitude,
                createdAt = missionPlace.createdAt,
                modifiedAt = missionPlace.modifiedAt
            )
        }
    }

    fun getMissionsByPlace(placeId: Long): List<site.honmoon.mission.dto.MissionDetailResponse> {
        val missions = missionDetailRepository.findByPlaceId(placeId)
        return missions.map { mission ->
            site.honmoon.mission.dto.MissionDetailResponse(
                id = mission.id,
                title = mission.title,
                description = mission.description,
                points = mission.points,
                missionType = mission.missionType,
                placeId = mission.placeId,
                question = mission.question,
                answer = mission.answer,
                choices = mission.choices,
                answerExplanation = mission.answerExplanation,
                correctImageUrl = mission.correctImageUrl,
                imageUploadInstruction = mission.imageUploadInstruction,
                createdAt = mission.createdAt,
                modifiedAt = mission.modifiedAt
            )
        }
    }
} 
