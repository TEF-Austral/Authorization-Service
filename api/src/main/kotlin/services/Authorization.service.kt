package services

import dtos.CheckPermissionRequestDTO
import dtos.GrantPermissionRequestDTO
import dtos.PermissionResponseDTO
import entities.Permission
import org.springframework.stereotype.Service
import repositories.PermissionRepository

@Service
class AuthorizationService(
    private val permissionRepository: PermissionRepository,
) {

    fun checkPermission(request: CheckPermissionRequestDTO): Boolean {
        val action = request.action.lowercase()

        when (action) {
            "create" -> return true

            "read" -> {
                if (request.ownerId == request.userId) return true
                return hasExplicitPermission(request.userId, request.snippetId, "read")
            }

            "edit", "update" -> {
                if (request.ownerId == request.userId) return true
                return hasExplicitPermission(request.userId, request.snippetId, "edit")
            }

            "delete", "share", "grant_permission" -> {
                return request.ownerId == request.userId
            }

            "execute", "run_test", "format", "analyze" -> {
                if (request.ownerId == request.userId) return true
                return hasExplicitPermission(request.userId, request.snippetId, "read")
            }

            else -> return false
        }
    }

    fun grantPermission(request: GrantPermissionRequestDTO): PermissionResponseDTO {
        if (request.requesterId != request.ownerId) {
            throw SecurityException("Only the owner can grant permissions")
        }

        if (request.granteeId == request.ownerId) {
            throw IllegalArgumentException("Cannot grant permissions to the owner")
        }

        val existing =
            permissionRepository.findByUserIdAndSnippetId(
                request.granteeId,
                request.snippetId,
            )

        val permission =
            if (existing != null) {
                existing.copy(
                    canRead = request.canRead,
                    canEdit = request.canEdit,
                )
            } else {
                Permission(
                    userId = request.granteeId,
                    snippetId = request.snippetId,
                    canRead = request.canRead,
                    canEdit = request.canEdit,
                )
            }

        val saved = permissionRepository.save(permission)
        return toDTO(saved)
    }

    fun revokePermission(
        userId: String,
        snippetId: String,
        requesterId: String,
    ) {
        val permission =
            permissionRepository.findByUserIdAndSnippetId(userId, snippetId)
                ?: throw IllegalArgumentException("Permission not found")

        permissionRepository.deleteByUserIdAndSnippetId(userId, snippetId)
    }

    fun getSnippetPermissions(
        snippetId: String,
        requesterId: String,
    ): List<PermissionResponseDTO> =
        permissionRepository.findAllBySnippetId(snippetId).map {
            toDTO(it)
        }

    fun getUserPermissions(userId: String): List<PermissionResponseDTO> =
        permissionRepository.findAllByUserId(userId).map {
            toDTO(it)
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

    private fun toDTO(permission: Permission): PermissionResponseDTO =
        PermissionResponseDTO(
            id = permission.id,
            userId = permission.userId,
            snippetId = permission.snippetId,
            canRead = permission.canRead,
            canEdit = permission.canEdit,
        )
}
