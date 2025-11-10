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

    fun checkPermission(request: CheckPermissionRequestDTO): Boolean =
        permissionChecker.isAllowed(request)

    fun grantPermission(request: GrantPermissionRequestDTO): PermissionResponseDTO =
        permissionManager.grant(request)

    fun revokePermission(
        userId: String,
        snippetId: String,
        requesterId: String,
    ) {
        permissionManager.revoke(userId, snippetId, requesterId)
    }

    fun getSnippetPermissions(
        snippetId: String,
        requesterId: String,
    ): List<PermissionResponseDTO> =
        permissionQueryService.getSnippetPermissions(snippetId, requesterId)

    fun getUserPermissions(userId: String): List<PermissionResponseDTO> =
        permissionQueryService.getUserPermissions(userId)

    fun getSnippetsByPermission(
        userId: String,
        permission: String,
    ): List<String> = permissionQueryService.getSnippetsByPermission(userId, permission)
}
