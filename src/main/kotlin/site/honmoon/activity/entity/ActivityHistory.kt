package site.honmoon.activity.entity

import jakarta.persistence.*
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity
import java.util.*

@Entity
@Table(
    name = "user_activity",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "place_id"], name = "unique_user_place")
    ]
)
class UserActivity(
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    
    @Column(name = "place_id", nullable = false)
    var placeId: Long,
    
    @Column(name = "mission_id")
    var missionId: Long? = null,
    
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,
    
    @Column(name = "is_correct")
    var isCorrect: Boolean? = null,
    
    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,
    
    @Column(name = "points_earned", nullable = false)
    var pointsEarned: Int = 0,
    
    @Column(name = "text_answer", columnDefinition = "TEXT")
    var textAnswer: String? = null,
    
    @Column(name = "selected_choice_index")
    var selectedChoiceIndex: Int? = null,
    
    @Column(name = "uploaded_image_url")
    var uploadedImageUrl: String? = null
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 