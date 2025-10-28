package repositories

import entities.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaPermissionRepository : JpaRepository<Permission, Long> {
    fun findByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    ): Permission?

    fun deleteByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    )

    fun findAllBySnippetId(snippetId: String): List<Permission>

    fun findAllByUserId(userId: String): List<Permission>
}
