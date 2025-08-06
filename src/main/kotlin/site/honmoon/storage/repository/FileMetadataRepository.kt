package site.honmoon.storage.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import site.honmoon.storage.entity.FileMetadata
import java.util.*

@Repository
interface FileMetadataRepository : JpaRepository<FileMetadata, UUID> {
    fun findByFileNameAndIsActiveTrue(fileName: String): FileMetadata?
    fun findByUserIdAndIsActiveTrue(userId: UUID): List<FileMetadata>
    fun findByUserIdAndFileNameAndIsActiveTrue(userId: UUID, fileName: String): FileMetadata?
}