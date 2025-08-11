package site.honmoon.mission.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.springframework.data.relational.core.mapping.Table
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity
import site.honmoon.mission.type.MissionType
import site.honmoon.mission.vo.MissionAnswerVo
import site.honmoon.mission.vo.MissionChoicesVo
import site.honmoon.mission.vo.MissionImageUploadVo

@Entity
@Table(name = "mission_detail")
class MissionDetail(
    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "points", nullable = false)
    var points: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false)
    var missionType: MissionType,

    @Column(name = "place_id")
    var placeId: Long? = null,

    @Column(name = "question", columnDefinition = "TEXT")
    var question: String? = null,

    @Type(JsonType::class)
    @Column(name = "answer", columnDefinition = "jsonb")
    var answer: MissionAnswerVo? = null,

    @Type(JsonType::class)
    @Column(name = "choices", columnDefinition = "jsonb")
    var choices: MissionChoicesVo? = null,

    @Type(JsonType::class)
    @Column(name = "image_upload", columnDefinition = "jsonb")
    var imageUpload: MissionImageUploadVo? = null,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 
