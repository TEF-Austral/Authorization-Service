package api.services.authorization

import api.dtos.responses.PermissionResponseDTO
import org.springframework.stereotype.Component
import repositories.PermissionRepository

@Component
class DefaultPermissionQueryService(
    private val permissionRepository: PermissionRepository,
    private val permissionMapper: PermissionMapper,
) : PermissionQueryService {
    private val log = org.slf4j.LoggerFactory.getLogger(DefaultPermissionQueryService::class.java)

    override fun getSnippetPermissions(
        snippetId: String,
        requesterId: String,
    ): List<PermissionResponseDTO> {
        log.info("Fetching all permissions for snippet $snippetId requested by $requesterId")
        val result =
            permissionRepository
                .findAllBySnippetId(snippetId)
                .map { permissionMapper.toDTO(it) }
        log.warn("Retrieved ${result.size} permissions for snippet $snippetId")
        return result
    }

    override fun getUserPermissions(userId: String): List<PermissionResponseDTO> {
        log.info("Fetching all permissions for user $userId")
        val result =
            permissionRepository
                .findAllByUserId(userId)
                .map { permissionMapper.toDTO(it) }
        log.warn("Retrieved ${result.size} permissions for user $userId")
        return result
    }

    override fun getSnippetsByPermission(
        userId: String,
        permission: String,
    ): List<String> {
        log.info("Fetching snippets with permission '$permission' for user $userId")
        val userPermissions = permissionRepository.findAllByUserId(userId)

        val result =
            when (permission.lowercase()) {
                "read" -> userPermissions.filter { it.canRead }.map { it.snippetId }
                "edit" -> userPermissions.filter { it.canEdit }.map { it.snippetId }
                else -> throw IllegalArgumentException(
                    "Invalid permission type. Must be 'read' or 'edit'",
                )
            }

        log.warn("Found ${result.size} snippets with permission '$permission' for user $userId")
        return result
    }
}
