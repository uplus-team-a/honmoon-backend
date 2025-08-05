package site.honmoon.sample.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import site.honmoon.sample.dto.SampleCreateRequest
import site.honmoon.sample.dto.SampleResponse
import site.honmoon.sample.dto.SampleUpdateRequest
import site.honmoon.sample.entity.SampleEntity
import site.honmoon.sample.repository.SampleRepository

@Service
class SampleService(
    private val sampleRepository: SampleRepository,
) {
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<SampleResponse> {
        return sampleRepository.findAll(pageable)
            .map { SampleResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): SampleResponse {
        val entity = sampleRepository.findByIdOrNull(id)
            ?: throw IllegalStateException("Sample entity not found with id: $id")
        return SampleResponse.from(entity)
    }

    @Transactional(readOnly = true)
    fun findByName(name: String): List<SampleResponse> {
        return sampleRepository.findByName(name)
            .map { SampleResponse.from(it) }
    }

    @Transactional
    fun create(request: SampleCreateRequest): SampleResponse {
        val entity = SampleEntity(
            name = request.name,
            description = request.description
        )
        val savedEntity = sampleRepository.save(entity)
        return SampleResponse.from(savedEntity)
    }

    @Transactional
    fun update(id: Long, request: SampleUpdateRequest): SampleResponse {
        val entity = sampleRepository.findByIdOrNull(id)
            ?: throw IllegalStateException("Sample entity not found with id: $id")

        request.name?.let { entity.name = it }
        request.description?.let { entity.description = it }

        val updatedEntity = sampleRepository.save(entity)
        return SampleResponse.from(updatedEntity)
    }

    @Transactional
    fun delete(id: Long) {
        if (!sampleRepository.existsById(id)) {
            throw IllegalStateException("Sample entity not found with id: $id")
        }
        sampleRepository.deleteById(id)
    }
}
