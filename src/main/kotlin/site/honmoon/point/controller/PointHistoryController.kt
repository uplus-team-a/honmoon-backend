package site.honmoon.point.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.point.dto.PointHistoryResponse
import site.honmoon.point.service.PointHistoryService
import java.util.*

@Tag(name = "Point History", description = "포인트 내역 관련 API")
@RestController
@RequestMapping("/api/point-history")
class PointHistoryController(
    private val pointHistoryService: PointHistoryService
) {

    @Operation(
        summary = "포인트 내역 상세 조회",
        description = "특정 포인트 내역의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    fun getPointHistory(
        @Parameter(description = "포인트 내역 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<PointHistoryResponse> {
        return Response.success(pointHistoryService.getPointHistory(id))
    }

    @Operation(
        summary = "사용자 포인트 내역 조회",
        description = "특정 사용자의 모든 포인트 획득/사용 내역을 조회합니다."
    )
    @GetMapping("/user/{userId}")
    fun getUserPointHistory(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<List<PointHistoryResponse>> {
        return Response.success(pointHistoryService.getUserPointHistory(userId))
    }

    @Operation(
        summary = "내 포인트 내역 조회",
        description = "현재 로그인한 사용자의 포인트 내역을 조회합니다."
    )
    @GetMapping("/me")
    fun getMyPointHistory(
        @CurrentUser currentUser: UserPrincipal
    ): Response<List<PointHistoryResponse>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(pointHistoryService.getUserPointHistory(userId))
    }

    @Operation(
        summary = "사용자 포인트 획득 내역 조회",
        description = "특정 사용자의 포인트 획득 내역만 조회합니다."
    )
    @GetMapping("/user/{userId}/earned")
    fun getUserEarnedPointHistory(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<List<PointHistoryResponse>> {
        return Response.success(pointHistoryService.getUserEarnedPointHistory(userId))
    }

    @Operation(
        summary = "사용자 포인트 사용 내역 조회",
        description = "특정 사용자의 포인트 사용 내역만 조회합니다."
    )
    @GetMapping("/user/{userId}/used")
    fun getUserUsedPointHistory(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable userId: UUID,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<List<PointHistoryResponse>> {
        return Response.success(pointHistoryService.getUserUsedPointHistory(userId))
    }

    @Operation(
        summary = "래플 응모 포인트 차감",
        description = "사용자가 래플에 응모할 때 포인트를 차감합니다."
    )
    @PostMapping("/use/raffle")
    fun usePointsForRaffle(
        @Parameter(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam userId: UUID,
        @Parameter(description = "래플 상품 ID", example = "1")
        @RequestParam raffleProductId: Long,
        @CurrentUser currentUser: UserPrincipal?
    ): Response<PointHistoryResponse> {
        return Response.success(pointHistoryService.usePointsForRaffle(userId, raffleProductId))
    }
} 