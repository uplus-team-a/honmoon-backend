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
import site.honmoon.raffle.dto.RaffleProductResponse
import site.honmoon.raffle.service.RaffleProductService

@Tag(name = "Raffle Product", description = "래플 상품 관련 API")
@RestController
@RequestMapping("/api/raffle-products")
class RaffleProductController(
    private val raffleProductService: RaffleProductService,
) {

    @Operation(
        summary = "래플 상품 상세 조회",
        description = "특정 래플 상품의 상세 정보를 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = RaffleProductResponse::class),
                examples = [ExampleObject(
                    name = "success",
                    value = "{\n  \"id\": 1,\n  \"name\": \"아이폰 15\",\n  \"description\": \"최신 스마트폰\",\n  \"imageUrl\": \"https://.../iphone.jpg\",\n  \"createdAt\": \"2024-05-01T12:00:00Z\"\n}"
                )]
            )]
        )]
    )
    @GetMapping("/{id}")
    fun getRaffleProduct(
        @Parameter(description = "래플 상품 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<RaffleProductResponse> {
        return Response.success(raffleProductService.getRaffleProduct(id))
    }

    @Operation(
        summary = "래플 상품 목록 조회",
        description = "모든 래플 상품 목록을 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = Array<RaffleProductResponse>::class),
                examples = [ExampleObject(
                    name = "success",
                    value = "[{\\n  \\\"id\\\": 1, \\n  \\\"name\\\": \\\"아이폰 15\\\"\\n}]"
                )]
            )]
        )]
    )
    @GetMapping
    fun getRaffleProducts(
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<List<RaffleProductResponse>> {
        return Response.success(raffleProductService.getRaffleProducts())
    }

    @Operation(
        summary = "래플 상품 검색",
        description = "상품명으로 래플 상품을 검색합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/search")
    fun searchRaffleProducts(
        @Parameter(description = "검색할 상품명", example = "아이폰")
        @RequestParam name: String,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<List<RaffleProductResponse>> {
        return Response.success(raffleProductService.searchRaffleProducts(name))
    }

    @Operation(
        summary = "포인트별 래플 상품 조회",
        description = "특정 포인트 범위의 래플 상품들을 조회합니다.",
        responses = [ApiResponse(responseCode = "200", description = "성공")]
    )
    @GetMapping("/by-points")
    fun getRaffleProductsByPoints(
        @Parameter(description = "최소 포인트", example = "100")
        @RequestParam minPoints: Int,
        @Parameter(description = "최대 포인트", example = "1000")
        @RequestParam maxPoints: Int,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<List<RaffleProductResponse>> {
        return Response.success(raffleProductService.getRaffleProductsByPoints(minPoints, maxPoints))
    }

    @Operation(
        summary = "래플 상품 응모자 수 조회",
        description = "특정 래플 상품에 응모한 사용자 수를 조회합니다.",
        responses = [ApiResponse(
            responseCode = "200",
            description = "성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = Map::class),
                examples = [ExampleObject(name = "success", value = "{\\n  \\\"applicantsCount\\\": 42\\n}")]
            )]
        )]
    )
    @GetMapping("/{id}/applicants-count")
    fun getApplicantsCount(
        @Parameter(description = "래플 상품 ID", example = "1")
        @PathVariable id: Long,
        @CurrentUser currentUser: UserPrincipal?,
    ): Response<Map<String, Int>> {
        return Response.success(raffleProductService.getApplicantsCount(id))
    }
} 
