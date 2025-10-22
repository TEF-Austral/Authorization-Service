package api.services

import api.entities.PermissionType
import api.repositories.SnippetRepository
import api.repositories.UserPermissionRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

data class Subject(
    val id: String,
)

@Service
class SnippetAuthorizationService(
    private val snippetRepository: SnippetRepository,
    private val permissionRepository: UserPermissionRepository,
) {

    fun checkPermission(
        subject: Subject,
        snippetId: Long,
        action: PermissionType,
    ): Boolean {
        // 1. Cargar el Snippet (el Objeto)
        val snippet =
            snippetRepository
                .findById(snippetId)
                .orElse(null) ?: return false // Denegar si el snippet no existe

        // 2. Política de Dueño (Owner): El dueño SIEMPRE tiene todos los permisos.
        if (snippet.ownerId == subject.id) {
            return true
        }

        // 3. Política de Permiso Explícito:
        // Si no es el dueño, consultar la tabla 'user_permissions'
        return permissionRepository.existsByUserIdAndSnippetIdAndPermission(
            userId = subject.id,
            snippetId = snippetId,
            permission = action,
        )
    }

    fun getSubjectFromJwt(jwt: Jwt): Subject =
        Subject(
            id = jwt.subject,
        )
}
