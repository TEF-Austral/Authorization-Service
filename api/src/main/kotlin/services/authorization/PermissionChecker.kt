package api.services.authorization

import api.dtos.requests.CheckPermissionRequestDTO

interface PermissionChecker {
    fun isAllowed(request: CheckPermissionRequestDTO): Boolean
}
