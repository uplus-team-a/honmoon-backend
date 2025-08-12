package site.honmoon.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import site.honmoon.user.entity.Users
import java.util.*

interface UsersRepository : JpaRepository<Users, UUID> {
    fun findByEmail(email: String): Users?
    fun findFirstByEmailIsNotNullOrderByEmailAsc(): Users?
}


