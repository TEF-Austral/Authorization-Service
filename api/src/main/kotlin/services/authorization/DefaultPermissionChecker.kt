package api.services.authorization

import api.dtos.requests.CheckPermissionRequestDTO
import org.springframework.stereotype.Component
import repositories.PermissionRepository

@Component
class DefaultPermissionChecker(
    private val permissionRepository: PermissionRepository,
) : PermissionChecker {
    private val log = org.slf4j.LoggerFactory.getLogger(DefaultPermissionChecker::class.java)

    override fun isAllowed(request: CheckPermissionRequestDTO): Boolean {
        log.info(
            "Checking if user ${request.userId} is allowed to perform ${request.action} on snippet ${request.snippetId}",
        )
        val action = request.action.lowercase()

        val result =
            when (action) {
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

        log.warn("Permission check for user ${request.userId}, action ${request.action}: $result")
        return result
    }

    private fun hasExplicitPermission(
        userId: String,
        snippetId: String,
        action: String,
    ): Boolean {
        log.info(
            "Checking explicit permission for user $userId on snippet $snippetId, action: $action",
        )
        val permission =
            permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
                ?: return false

        val result =
            when (action) {
                "read" -> permission.canRead
                "edit" -> permission.canEdit
                else -> false
            }

        log.warn("Explicit permission check for user $userId: $result")
        return result
    }
}
