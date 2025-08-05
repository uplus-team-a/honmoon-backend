package site.honmoon.sample.dto

import site.honmoon.sample.entity.SampleEntity
import java.time.Instant

data class SampleResponse(
    val id: Long?,
    val name: String?,
    val description: String?,
    val createdBy: String?,
    val createdAt: Instant,
    val modifiedBy: String?,
    val modifiedAt: Instant,
) {
    companion object {
        fun from(entity: SampleEntity): SampleResponse {
            return SampleResponse(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                createdBy = entity.createdBy,
                createdAt = entity.createdAt,
                modifiedBy = entity.modifiedBy,
                modifiedAt = entity.modifiedAt
            )
        }
    }
}

data class SampleCreateRequest(
    val name: String,
    val description: String?,
)

data class SampleUpdateRequest(
    val name: String?,
    val description: String?,
)
