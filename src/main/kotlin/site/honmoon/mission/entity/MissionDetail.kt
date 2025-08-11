package site.honmoon.mission.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.springframework.data.relational.core.mapping.Table
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity
import site.honmoon.mission.type.MissionType
import site.honmoon.mission.vo.MissionChoicesVo

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

    @Column(name = "answer", columnDefinition = "TEXT")
    var answer: String? = null,

    @Type(JsonType::class)
    @Column(name = "choices", columnDefinition = "jsonb")
    var choices: MissionChoicesVo? = null,

    @Column(name = "answer_explanation", columnDefinition = "TEXT")
    var answerExplanation: String? = null,

    @Column(name = "correct_image_url")
    var correctImageUrl: String? = null,

    @Column(name = "image_upload_instruction", columnDefinition = "TEXT")
    var imageUploadInstruction: String? = null,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 
