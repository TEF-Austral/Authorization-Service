package api.services.authorization

import api.dtos.responses.PermissionResponseDTO

interface PermissionQueryService {
    fun getSnippetPermissions(
        snippetId: String,
        requesterId: String,
    ): List<PermissionResponseDTO>

    fun getUserPermissions(userId: String): List<PermissionResponseDTO>

    fun getSnippetsByPermission(
        userId: String,
        permission: String,
    ): List<String>
}
