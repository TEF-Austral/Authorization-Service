package api.services

import api.repositories.PermissionRepository
import api.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val userRepository: UserRepository
) {

    fun checkPermission(
        userId: String,
        resource: String,
        action: String,
        resourceId: String? = null
    ): Boolean {
        // Buscar usuario por auth0Id
        val user = userRepository.findByAuth0Id(userId) ?: return false

        // Verificar permiso general
        val hasGeneralPermission = permissionRepository.userHasPermission(
            userId = user.id!!,
            resource = resource,
            action = action
        )

        if (!hasGeneralPermission) {
            return false
        }

        // Si se especifica resourceId, verificar permisos específicos
        if (resourceId != null) {
            return checkSpecificResourcePermission(user.id!!, resource, action, resourceId)
        }

        return true
    }

    private fun checkSpecificResourcePermission(
        userId: Long,
        resource: String,
        action: String,
        resourceId: String
    ): Boolean {
        // Lógica ABAC para recursos específicos
        return when (resource) {
            "users" -> {
                if (action in listOf("update", "delete")) {
                    // Solo admin o el mismo usuario
                    val isAdmin = permissionRepository.userHasRole(userId, "admin")
                    isAdmin || userId.toString() == resourceId
                } else {
                    true
                }
            }
            else -> true
        }
    }
}
