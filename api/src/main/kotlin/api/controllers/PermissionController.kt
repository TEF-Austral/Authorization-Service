package api.controllers

import api.dtos.PermissionCheckRequest
import api.dtos.PermissionCheckResponse
import api.services.PermissionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/permissions")
class PermissionController(
    private val permissionService: PermissionService
) {

    @PostMapping("/check")
    fun checkPermission(
        @RequestBody request: PermissionCheckRequest
    ): ResponseEntity<PermissionCheckResponse> {
        val hasPermission = permissionService.checkPermission(
            userId = request.userId,
            resource = request.resource,
            action = request.action,
            resourceId = request.resourceId
        )

        return ResponseEntity.ok(
            PermissionCheckResponse(
                hasPermission = hasPermission,
                reason = if (!hasPermission) "User does not have permission" else null
            )
        )
    }
}
