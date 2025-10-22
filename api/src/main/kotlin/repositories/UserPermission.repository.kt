package api.repositories

import api.entities.PermissionType
import api.entities.UserPermission
import org.springframework.data.jpa.repository.JpaRepository

interface UserPermissionRepository : JpaRepository<UserPermission, Long> {
    fun existsByUserIdAndSnippetIdAndPermission(
        userId: String,
        snippetId: Long,
        permission: PermissionType,
    ): Boolean
}
