package site.honmoon.raffle.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import site.honmoon.auth.security.CurrentUser
import site.honmoon.auth.security.UserPrincipal
import site.honmoon.common.Response
import site.honmoon.raffle.dto.RaffleApplyResult
import site.honmoon.raffle.dto.RaffleUserApplicationResponse
import site.honmoon.raffle.service.RaffleUserApplicationService
import java.util.*

@Tag(name = "Raffle User Application", description = "래플 응모 관련 API")
@RestController
@RequestMapping("/api/raffle-applications")
class RaffleUserApplicationController(
    private val raffleUserApplicationService: RaffleUserApplicationService,
) {

    @Operation(
        summary = "래플 응모 상세 조회",
        description = "특정 래플 응모의 상세 정보를 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = RaffleUserApplicationResponse::class),
                examples = [ExampleObject(
                    name = "success",
                    value = "{\n  \"id\": 1,\n  \"userId\": \"123e4567-e89b-12d3-a456-426614174000\",\n  \"raffleProductId\": 1,\n  \"applicationDate\": \"2024-05-01T12:00:00Z\"\n}"
                )]
            )]
        )]
    )
    @GetMapping("/{id}")
    fun getRaffleUserApplication(
        @Parameter(description = "래플 응모 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<RaffleUserApplicationResponse> {
        return Response.success(raffleUserApplicationService.getRaffleUserApplication(id))
    }


    @Operation(
        summary = "내 래플 응모 내역 조회",
        description = "현재 로그인한 사용자의 래플 응모 내역을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me")
    fun getMyRaffleApplications(
        @CurrentUser currentUser: UserPrincipal,
    ): Response<List<RaffleUserApplicationResponse>> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(raffleUserApplicationService.getUserRaffleApplications(userId))
    }

    @Operation(
        summary = "래플 상품별 응모자 목록 조회",
        description = "특정 래플 상품에 응모한 모든 사용자 목록을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/product/{productId}")
    fun getRaffleApplicationsByProduct(
        @Parameter(description = "래플 상품 ID", example = "1")
        @PathVariable productId: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<List<RaffleUserApplicationResponse>> {
        return Response.success(raffleUserApplicationService.getRaffleApplicationsByProduct(productId))
    }

    @Operation(
        summary = "래플 응모",
        description = "사용자가 특정 래플 상품에 응모합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = RaffleUserApplicationResponse::class),
                examples = [ExampleObject(name = "success", value = "{\n  \"raffleProductId\": 1\n}")]
            )]
        )]
    )
    @PostMapping
    fun applyRaffle(
        @Parameter(description = "래플 상품 ID", example = "1")
        @RequestParam raffleProductId: Long,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<RaffleApplyResult> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(raffleUserApplicationService.applyRaffleWithStatus(userId, raffleProductId))
    }

    @Operation(
        summary = "내 래플 응모",
        description = "현재 로그인한 사용자가 특정 래플 상품에 응모합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/me")
    fun applyRaffleMe(
        @Parameter(description = "래플 상품 ID", example = "1")
        @RequestParam raffleProductId: Long,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<RaffleApplyResult> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(raffleUserApplicationService.applyRaffleWithStatus(userId, raffleProductId))
    }

    @Operation(
        summary = "래플 당첨자 선정",
        description = "관리자가 특정 래플 상품의 당첨자를 선정합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @PostMapping("/{productId}/draw")
    fun drawRaffleWinners(
        @Parameter(description = "래플 상품 ID", example = "1")
        @PathVariable productId: Long,
        @Parameter(description = "당첨자 수", example = "1")
        @RequestParam winnerCount: Int = 1,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<List<RaffleUserApplicationResponse>> {
        return Response.success(raffleUserApplicationService.drawRaffleWinners(productId, winnerCount))
    }


    @Operation(
        summary = "내 응모 상태 조회",
        description = "현재 로그인한 사용자가 특정 래플 상품에 응모했는지 확인합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/me/product/{productId}")
    fun getMyApplicationStatus(
        @Parameter(description = "래플 상품 ID", example = "1")
        @PathVariable productId: Long,
        @CurrentUser currentUser: UserPrincipal,
    ): Response<RaffleUserApplicationResponse?> {
        val userId = UUID.fromString(currentUser.subject)
        return Response.success(raffleUserApplicationService.getUserApplicationStatus(userId, productId))
    }
} 
