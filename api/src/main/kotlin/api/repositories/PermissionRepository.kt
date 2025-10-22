package api.repositories

import api.entities.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM User u
        JOIN u.roles r
        JOIN r.permissions p
        WHERE u.id = :userId
        AND p.resource = :resource
        AND p.action = :action
    """)
    fun userHasPermission(
        userId: Long,
        resource: String,
        action: String
    ): Boolean

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM User u
        JOIN u.roles r
        WHERE u.id = :userId
        AND r.name = :roleName
    """)
    fun userHasRole(userId: Long, roleName: String): Boolean
}
