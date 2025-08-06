package site.honmoon.mission.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.mission.dto.MissionPlaceResponse
import site.honmoon.mission.service.MissionPlaceService

@Tag(name = "Mission Place", description = "미션 장소 관련 API")
@RestController
@RequestMapping("/api/mission-places")
class MissionPlaceController(
    private val missionPlaceService: MissionPlaceService
) {

    @Operation(
        summary = "미션 장소 상세 조회",
        description = "특정 미션 장소의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    fun getMissionPlace(
        @Parameter(description = "미션 장소 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<MissionPlaceResponse> {
        return Response.success(missionPlaceService.getMissionPlace(id))
    }

    @Operation(
        summary = "미션 장소 목록 조회",
        description = "모든 미션 장소 목록을 조회합니다."
    )
    @GetMapping
    fun getMissionPlaces(
        @CurrentUser currentUser: UserPrincipal?
    ): Response<List<MissionPlaceResponse>> {
        return Response.success(missionPlaceService.getMissionPlaces())
    }

    @Operation(
        summary = "미션 장소 검색",
        description = "장소명으로 미션 장소를 검색합니다."
    )
    @GetMapping("/search")
    fun searchMissionPlaces(
        @Parameter(description = "검색할 장소명", example = "한강공원")
        @RequestParam title: String,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<List<MissionPlaceResponse>> {
        return Response.success(missionPlaceService.searchMissionPlaces(title))
    }

    @Operation(
        summary = "근처 미션 장소 조회",
        description = "사용자 위치 기준으로 근처의 미션 장소들을 조회합니다."
    )
    @GetMapping("/nearby")
    fun getNearbyMissionPlaces(
        @Parameter(description = "사용자 위도", example = "37.5665")
        @RequestParam lat: Double,
        @Parameter(description = "사용자 경도", example = "126.9780")
        @RequestParam lng: Double,
        @Parameter(description = "검색 반경 (미터)", example = "1000")
        @RequestParam radius: Int = 1000,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<List<MissionPlaceResponse>> {
        return Response.success(missionPlaceService.getNearbyMissionPlaces(lat, lng, radius))
    }

    @Operation(
        summary = "미션 장소별 미션 목록 조회",
        description = "특정 미션 장소에 속한 모든 미션 목록을 조회합니다."
    )
    @GetMapping("/{id}/missions")
    fun getMissionsByPlace(
        @Parameter(description = "미션 장소 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<List<site.honmoon.mission.dto.MissionSummaryResponse>> {
        return Response.success(missionPlaceService.getMissionsByPlace(id))
    }
} 