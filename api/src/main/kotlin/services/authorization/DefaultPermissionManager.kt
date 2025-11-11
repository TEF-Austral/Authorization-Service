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
    private val log = org.slf4j.LoggerFactory.getLogger(DefaultPermissionManager::class.java)

    override fun grant(request: GrantPermissionRequestDTO): PermissionResponseDTO {
        log.info(
            "Granting permission to user ${request.granteeId} for snippet ${request.snippetId}",
        )
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
        log.warn("Permission granted successfully for user ${request.granteeId}")
        return permissionMapper.toDTO(saved)
    }

    override fun revoke(
        userId: String,
        snippetId: String,
        requesterId: String,
    ) {
        log.info(
            "Revoking permission for user $userId on snippet $snippetId by requester $requesterId",
        )
        permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
            ?: throw IllegalArgumentException("Permission not found")

        permissionRepository.deleteByUserIdAndSnippetId(userId, snippetId)
        log.warn("Permission revoked successfully for user $userId")
    }

    private fun validateGrantRequest(request: GrantPermissionRequestDTO) {
        log.info("Validating grant request from requester ${request.requesterId}")
        if (request.requesterId != request.ownerId) {
            log.warn(
                "Unauthorized grant attempt: requester ${request.requesterId} is not the owner",
            )
            throw SecurityException("Only the owner can grant permissions")
        }

        if (request.granteeId == request.ownerId) {
            log.warn("Invalid grant attempt: trying to grant permissions to the owner")
            throw IllegalArgumentException("Cannot grant permissions to the owner")
        }
    }
}
