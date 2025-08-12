package site.honmoon.storage.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "file_metadata")
@EntityListeners(AuditingEntityListener::class)
data class FileMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "file_name", nullable = false)
    val fileName: String,

    @Column(name = "original_name", nullable = false)
    val originalName: String,

    @Column(name = "file_url", nullable = false)
    val fileUrl: String,

    @Column(name = "file_size")
    val fileSize: Long? = null,

    @Column(name = "content_type")
    val contentType: String? = null,

    @Column(name = "folder", nullable = false)
    val folder: String = "images",

    @Column(name = "created_by", nullable = false)
    val createdBy: String = "system",

    @Column(name = "modified_by", nullable = false)
    val modifiedBy: String = "system",

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime? = null,

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    val modifiedAt: OffsetDateTime? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
)
