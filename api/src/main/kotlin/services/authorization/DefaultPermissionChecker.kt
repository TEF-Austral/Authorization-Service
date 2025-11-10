package api.services.authorization

import api.dtos.requests.CheckPermissionRequestDTO
import org.springframework.stereotype.Component
import repositories.PermissionRepository

@Component
class DefaultPermissionChecker(
    private val permissionRepository: PermissionRepository,
) : PermissionChecker {

    override fun isAllowed(request: CheckPermissionRequestDTO): Boolean {
        val action = request.action.lowercase()

        return when (action) {
            "create" -> true

            "read" -> {
                if (request.ownerId == request.userId) return true
                hasExplicitPermission(request.userId, request.snippetId, "read")
            }

            "edit", "update" -> {
                if (request.ownerId == request.userId) return true
                hasExplicitPermission(request.userId, request.snippetId, "edit")
            }

            "delete", "share", "grant_permission" -> {
                request.ownerId == request.userId
            }

            "execute", "run_test", "format", "analyze" -> {
                if (request.ownerId == request.userId) return true
                hasExplicitPermission(request.userId, request.snippetId, "read")
            }

            else -> false
        }
    }

    private fun hasExplicitPermission(
        userId: String,
        snippetId: String,
        action: String,
    ): Boolean {
        val permission =
            permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
                ?: return false

        return when (action) {
            "read" -> permission.canRead
            "edit" -> permission.canEdit
            else -> false
        }
    }
}
