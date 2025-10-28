package repositories

import entities.Permission
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DefaultPermissionRepository(
    private val jpaPermissionRepository: JpaPermissionRepository,
) : PermissionRepository {

    override fun findByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    ): Permission? = jpaPermissionRepository.findByUserIdAndSnippetId(userId, snippetId)

    override fun save(permission: Permission): Permission = jpaPermissionRepository.save(permission)

    @Transactional
    override fun deleteByUserIdAndSnippetId(
        userId: String,
        snippetId: String,
    ) {
        jpaPermissionRepository.deleteByUserIdAndSnippetId(userId, snippetId)
    }

    override fun findAllBySnippetId(snippetId: String): List<Permission> =
        jpaPermissionRepository.findAllBySnippetId(snippetId)

    override fun findAllByUserId(userId: String): List<Permission> =
        jpaPermissionRepository.findAllByUserId(userId)
}
