package api.services.authorization

import api.dtos.requests.GrantPermissionRequestDTO
import api.dtos.responses.PermissionResponseDTO

interface PermissionManager {
    fun grant(request: GrantPermissionRequestDTO): PermissionResponseDTO

    fun revoke(
        userId: String,
        snippetId: String,
        requesterId: String,
    )
}
