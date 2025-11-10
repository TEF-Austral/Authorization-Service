package api.services.authorization

import api.dtos.responses.PermissionResponseDTO
import org.springframework.stereotype.Component
import repositories.PermissionRepository

@Component
class DefaultPermissionQueryService(
    private val permissionRepository: PermissionRepository,
    private val permissionMapper: PermissionMapper,
) : PermissionQueryService {

    override fun getSnippetPermissions(
        snippetId: String,
        requesterId: String,
    ): List<PermissionResponseDTO> =
        permissionRepository
            .findAllBySnippetId(snippetId)
            .map { permissionMapper.toDTO(it) }

    override fun getUserPermissions(userId: String): List<PermissionResponseDTO> =
        permissionRepository
            .findAllByUserId(userId)
            .map { permissionMapper.toDTO(it) }

    override fun getSnippetsByPermission(
        userId: String,
        permission: String,
    ): List<String> {
        val userPermissions = permissionRepository.findAllByUserId(userId)

        return when (permission.lowercase()) {
            "read" -> userPermissions.filter { it.canRead }.map { it.snippetId }
            "edit" -> userPermissions.filter { it.canEdit }.map { it.snippetId }
            else -> throw IllegalArgumentException(
                "Invalid permission type. Must be 'read' or 'edit'",
            )
        }
    }
}
