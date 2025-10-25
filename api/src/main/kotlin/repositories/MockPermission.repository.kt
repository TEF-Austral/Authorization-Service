package repositories

import entities.Permission

class MockPermissionRepository : PermissionRepository {
    private val permissions = mutableMapOf<Pair<String, String>, Permission>()
    private var idCounter = 1L

    override fun findByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    ): Permission? = permissions[Pair(userId, snippetId)]

    override fun save(permission: Permission): Permission {
        val savedPermission =
            if (permission.id == null) {
                permission.copy(id = idCounter++)
            } else {
                permission
            }
        permissions[Pair(savedPermission.userId, savedPermission.snippetId)] = savedPermission
        return savedPermission
    }

    override fun deleteByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    ) {
        permissions.remove(Pair(userId, snippetId))
    }

    override fun findAllBySnippetId(snippetId: String): List<Permission> =
        permissions.values.filter {
            it.snippetId == snippetId
        }

    override fun findAllByUserId(userId: String): List<Permission> =
        permissions.values.filter {
            it.userId == userId
        }

    fun clear() {
        permissions.clear()
        idCounter = 1L
    }
}
