package api.services.authorization

import api.dtos.responses.PermissionResponseDTO
import entities.Permission
import org.springframework.stereotype.Component

@Component
class PermissionMapper {

    fun toDTO(permission: Permission): PermissionResponseDTO =
        PermissionResponseDTO(
            id = permission.id,
            userId = permission.userId,
            snippetId = permission.snippetId,
            canRead = permission.canRead,
            canEdit = permission.canEdit,
        )
}
