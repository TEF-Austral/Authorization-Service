package api.services.authorization

import api.dtos.requests.GrantPermissionRequestDTO
import api.dtos.responses.PermissionResponseDTO
import entities.Permission
import org.springframework.stereotype.Component
import repositories.PermissionRepository

@Component
class DefaultPermissionManager(
    private val permissionRepository: PermissionRepository,
    private val permissionMapper: PermissionMapper,
) : PermissionManager {

    override fun grant(request: GrantPermissionRequestDTO): PermissionResponseDTO {
        validateGrantRequest(request)

        val existing =
            permissionRepository.findByUserIdAndSnippetId(
                request.granteeId,
                request.snippetId,
            )

        val permission =
            existing?.copy(
                canRead = request.canRead,
                canEdit = request.canEdit,
            )
                ?: Permission(
                    userId = request.granteeId,
                    snippetId = request.snippetId,
                    canRead = request.canRead,
                    canEdit = request.canEdit,
                )

        val saved = permissionRepository.save(permission)
        return permissionMapper.toDTO(saved)
    }

    override fun revoke(
        userId: String,
        snippetId: String,
        requesterId: String,
    ) {
        permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
            ?: throw IllegalArgumentException("Permission not found")

        permissionRepository.deleteByUserIdAndSnippetId(userId, snippetId)
    }

    private fun validateGrantRequest(request: GrantPermissionRequestDTO) {
        if (request.requesterId != request.ownerId) {
            throw SecurityException("Only the owner can grant permissions")
        }

        if (request.granteeId == request.ownerId) {
            throw IllegalArgumentException("Cannot grant permissions to the owner")
        }
    }
}
