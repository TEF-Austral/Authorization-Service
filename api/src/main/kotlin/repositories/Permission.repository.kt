package repositories

import entities.Permission

interface PermissionRepository {
    fun findByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    ): Permission?

    fun save(permission: Permission): Permission

    fun deleteByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    )

    fun findAllBySnippetId(snippetId: String): List<Permission>

    fun findAllByUserId(userId: String): List<Permission>
}
