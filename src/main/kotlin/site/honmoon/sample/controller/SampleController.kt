package site.honmoon.sample.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import site.honmoon.common.ApiResponse
import site.honmoon.common.PageResponse
import site.honmoon.sample.dto.SampleCreateRequest
import site.honmoon.sample.dto.SampleResponse
import site.honmoon.sample.dto.SampleUpdateRequest
import site.honmoon.sample.service.SampleService

@RestController
@RequestMapping("/api/samples")
@Tag(name = "Sample", description = "Sample API")
class SampleController(
    private val sampleService: SampleService,
) {
    @GetMapping
    @Operation(summary = "Get all samples", description = "Returns a paginated list of samples")
    fun getAllSamples(@PageableDefault(size = 20) pageable: Pageable): ResponseEntity<ApiResponse<PageResponse<SampleResponse>>> {
        val page: Page<SampleResponse> = sampleService.findAll(pageable)
        val pageResponse = PageResponse(
            content = page.content,
            page = page.number,
            size = page.size,
            totalSize = page.totalElements,
            totalPages = page.totalPages
        )
        return ApiResponse.success(pageResponse)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sample by ID", description = "Returns a sample by its ID")
    fun getSampleById(@PathVariable id: Long): ResponseEntity<ApiResponse<SampleResponse>> {
        val sample = sampleService.findById(id)
        return ApiResponse.success(sample)
    }

    @GetMapping("/by-name")
    @Operation(summary = "Get samples by name", description = "Returns samples matching the given name")
    fun getSamplesByName(@RequestParam name: String): ResponseEntity<ApiResponse<List<SampleResponse>>> {
        val samples = sampleService.findByName(name)
        return ApiResponse.success(samples)
    }

    @PostMapping
    @Operation(summary = "Create sample", description = "Creates a new sample")
    fun createSample(@RequestBody request: SampleCreateRequest): ResponseEntity<ApiResponse<SampleResponse>> {
        val createdSample = sampleService.create(request)
        return ApiResponse.created(createdSample)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update sample", description = "Updates an existing sample")
    fun updateSample(
        @PathVariable id: Long,
        @RequestBody request: SampleUpdateRequest,
    ): ResponseEntity<ApiResponse<SampleResponse>> {
        val updatedSample = sampleService.update(id, request)
        return ApiResponse.success(updatedSample)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sample", description = "Deletes a sample")
    fun deleteSample(@PathVariable id: Long): ResponseEntity<ApiResponse<Void>> {
        sampleService.delete(id)
        return ApiResponse.noContent()
    }
}
