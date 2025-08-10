package site.honmoon.mission.entity

import jakarta.persistence.*
import site.honmoon.common.Constant
import site.honmoon.common.entity.BaseEntity

@Entity
@Table(name = "mission_place")
class MissionPlace(
    @Column(name = "name", nullable = false)
    var name: String,
    
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,
    
    @Column(name = "location", columnDefinition = "TEXT")
    var location: String? = null,
    
    @Column(name = "image")
    var image: String? = null,

    @Column(name = "latitude")
    var latitude: Double? = null,

    @Column(name = "longitude")
    var longitude: Double? = null,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = Constant.DB_NULL_ID
} 