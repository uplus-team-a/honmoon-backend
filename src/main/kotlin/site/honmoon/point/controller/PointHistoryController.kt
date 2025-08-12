package site.honmoon.point.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.point.dto.PointHistoryResponse
import site.honmoon.point.dto.UsePointsResult
import site.honmoon.point.service.PointHistoryService
import java.util.UUID

@Tag(name = "Point History", description = "포인트 내역 관련 API")
@RestController
@RequestMapping("/api/point-history")
class PointHistoryController(
    private val pointHistoryService: PointHistoryService,
) {

    @Operation(
        summary = "포인트 내역 상세 조회",
        description = "특정 포인트 내역의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    fun getPointHistory(
        @Parameter(description = "포인트 내역 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<PointHistoryResponse> {
        return Response.success(pointHistoryService.getPointHistory(id))
    }

    @Operation(
        summary = "내 포인트 내역 조회",
        description = "현재 로그인한 사용자의 포인트 내역을 조회합니다."
    )
    @GetMapping("/me")
    fun getMyPointHistory(
        @CurrentUser currentUser: UserPrincipal,
    ): Response<List<PointHistoryResponse>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(pointHistoryService.getUserPointHistory(userId))
    }

    @Operation(
        summary = "내 포인트 획득 내역 조회",
        description = "현재 로그인한 사용자의 포인트 획득 내역만 조회합니다."
    )
    @GetMapping("/me/earned")
    fun getMyEarnedPointHistory(
        @CurrentUser currentUser: UserPrincipal,
    ): Response<List<PointHistoryResponse>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(pointHistoryService.getUserEarnedPointHistory(userId))
    }

    @Operation(
        summary = "내 포인트 사용 내역 조회",
        description = "현재 로그인한 사용자의 포인트 사용 내역만 조회합니다."
    )
    @GetMapping("/me/used")
    fun getMyUsedPointHistory(
        @CurrentUser currentUser: UserPrincipal,
    ): Response<List<PointHistoryResponse>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(pointHistoryService.getUserUsedPointHistory(userId))
    }



    @Operation(
        summary = "포인트 사용 가능 여부 확인",
        description = "필요 포인트 대비 현재 포인트로 사용 가능 여부를 200 응답으로 반환합니다."
    )
    @GetMapping("/use/check")
    fun checkUsable(
        @RequestParam requiredPoints: Int,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<UsePointsResult> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(pointHistoryService.tryUsePoints(userId, requiredPoints))
    }
} 
