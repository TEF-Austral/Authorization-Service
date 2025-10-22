package api.repositories

import api.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByAuth0Id(auth0Id: String): User?
    fun findByEmail(email: String): User?
}
