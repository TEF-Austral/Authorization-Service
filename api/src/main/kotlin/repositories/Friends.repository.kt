package repositories

import entities.Friends
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FriendsRepository : JpaRepository<Friends, Long> {

    fun findByUserId1AndUserId2(
        userId1: String,
        userId2: String,
    ): Friends?

    fun findAllByUserId1(userId1: String): List<Friends>

    fun findAllByUserId2(userId2: String): List<Friends>

    fun deleteByUserId1AndUserId2(
        userId1: String,
        userId2: String,
    )

    fun existsByUserId1AndUserId2(
        userId1: String,
        userId2: String,
    ): Boolean
}
