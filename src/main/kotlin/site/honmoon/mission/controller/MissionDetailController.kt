package site.honmoon.mission.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.mission.dto.MissionDetailResponse
import site.honmoon.mission.service.MissionDetailService

@Tag(name = "Mission Detail", description = "미션 상세 정보 관련 API")
@RestController
@RequestMapping("/api/missions")
class MissionDetailController(
    private val missionDetailService: MissionDetailService
) {
    @Operation(
        summary = "미션 상세 정보 조회",
        description = "특정 미션의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    fun getMissionDetail(
        @Parameter(description = "미션 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<MissionDetailResponse> {
        return Response.success(missionDetailService.getMissionDetail(id))
    }
} 