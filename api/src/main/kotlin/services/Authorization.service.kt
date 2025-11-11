package api.services

import api.dtos.requests.CheckPermissionRequestDTO
import api.dtos.requests.GrantPermissionRequestDTO
import api.dtos.responses.PermissionResponseDTO
import api.services.authorization.PermissionChecker
import api.services.authorization.PermissionManager
import api.services.authorization.PermissionQueryService
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val permissionChecker: PermissionChecker,
    private val permissionManager: PermissionManager,
    private val permissionQueryService: PermissionQueryService,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(AuthorizationService::class.java)

    fun checkPermission(request: CheckPermissionRequestDTO): Boolean {
        log.info(
            "Checking permission for user ${request.userId} on snippet ${request.snippetId}, action: ${request.action}",
        )
        val result = permissionChecker.isAllowed(request)
        log.warn("Permission check result for user ${request.userId}: $result")
        return result
    }

    fun grantPermission(request: GrantPermissionRequestDTO): PermissionResponseDTO {
        log.info(
            "Granting permission to user ${request.granteeId} for snippet ${request.snippetId}",
        )
        val result = permissionManager.grant(request)
        log.warn("Permission granted successfully")
        return result
    }

    fun revokePermission(
        userId: String,
        snippetId: String,
        requesterId: String,
    ) {
        log.info(
            "Revoking permission for user $userId on snippet $snippetId by requester $requesterId",
        )
        permissionManager.revoke(userId, snippetId, requesterId)
        log.warn("Permission revoked successfully")
    }

    fun getSnippetPermissions(
        snippetId: String,
        requesterId: String,
    ): List<PermissionResponseDTO> {
        log.info("Fetching snippet permissions for snippet $snippetId by requester $requesterId")
        val result = permissionQueryService.getSnippetPermissions(snippetId, requesterId)
        log.warn("Retrieved ${result.size} permissions for snippet $snippetId")
        return result
    }

    fun getUserPermissions(userId: String): List<PermissionResponseDTO> {
        log.info("Fetching user permissions for user $userId")
        val result = permissionQueryService.getUserPermissions(userId)
        log.warn("Retrieved ${result.size} permissions for user $userId")
        return result
    }

    fun getSnippetsByPermission(
        userId: String,
        permission: String,
    ): List<String> {
        log.info("Fetching snippets by permission $permission for user $userId")
        val result = permissionQueryService.getSnippetsByPermission(userId, permission)
        log.warn("Retrieved ${result.size} snippets for user $userId with permission $permission")
        return result
    }
}
