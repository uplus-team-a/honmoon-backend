package site.honmoon.common.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity {
    @CreatedBy
    @Column(name = "created_by", nullable = false)
    var createdBy: String = "anonymous"

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @LastModifiedBy
    @Column(name = "modified_by", nullable = false)
    var modifiedBy: String = "anonymous"

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    var modifiedAt: Instant = Instant.now()
}
