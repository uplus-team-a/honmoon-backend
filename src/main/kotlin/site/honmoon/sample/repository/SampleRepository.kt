package site.honmoon.sample.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.sample.entity.SampleEntity

@Repository
interface SampleRepository : JpaRepository<SampleEntity, Long> {
    fun findByName(name: String): List<SampleEntity>
}
