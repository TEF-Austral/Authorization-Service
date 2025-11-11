package api.controllers

import api.dtos.requests.GetSnippetPermissionsRequestDTO
import api.dtos.requests.CheckPermissionRequestDTO
import api.dtos.responses.CheckPermissionResponseDTO
import api.dtos.requests.GrantPermissionRequestDTO
import api.dtos.requests.RevokePermissionRequestDTO
import api.dtos.responses.PermissionResponseDTO
import api.services.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/authorization")
class AuthorizationController(
    private val authorizationService: AuthorizationService,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(AuthorizationController::class.java)

    @PostMapping("/check")
    fun checkPermission(
        @RequestBody request: CheckPermissionRequestDTO,
    ): ResponseEntity<CheckPermissionResponseDTO> {
        log.info("POST /check - Checking permission for user ${request.userId}")
        val allowed = authorizationService.checkPermission(request)
        log.warn("POST /check - Permission check completed with result: $allowed")
        return ResponseEntity.ok(CheckPermissionResponseDTO(allowed))
    }

    @PostMapping("/permissions")
    fun grantPermission(
        @RequestBody request: GrantPermissionRequestDTO,
    ): ResponseEntity<PermissionResponseDTO> {
        log.info("POST /permissions - Granting permission to user ${request.granteeId}")
        val permission = authorizationService.grantPermission(request)
        log.warn("POST /permissions - Permission granted successfully")
        return ResponseEntity.ok(permission)
    }

    @PostMapping("/permissions/revoke")
    fun revokePermission(
        @RequestBody request: RevokePermissionRequestDTO,
    ): ResponseEntity<Void> {
        log.info("POST /permissions/revoke - Revoking permission for user ${request.userId}")
        authorizationService.revokePermission(
            request.userId,
            request.snippetId,
            request.requesterId,
        )
        log.warn("POST /permissions/revoke - Permission revoked successfully")
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/permissions/snippet")
    fun getSnippetPermissions(
        @RequestBody request: GetSnippetPermissionsRequestDTO,
    ): ResponseEntity<List<PermissionResponseDTO>> {
        log.info(
            "POST /permissions/snippet - Fetching permissions for snippet ${request.snippetId}",
        )
        val permissions =
            authorizationService.getSnippetPermissions(
                request.snippetId,
                request.requesterId,
            )
        log.warn("POST /permissions/snippet - Retrieved ${permissions.size} permissions")
        return ResponseEntity.ok(permissions)
    }

    @GetMapping("/permissions/user/{userId}")
    fun getUserPermissions(
        @PathVariable userId: String,
    ): ResponseEntity<List<PermissionResponseDTO>> {
        log.info("GET /permissions/user/$userId - Fetching user permissions")
        val permissions = authorizationService.getUserPermissions(userId)
        log.warn("GET /permissions/user/$userId - Retrieved ${permissions.size} permissions")
        return ResponseEntity.ok(permissions)
    }

    @GetMapping("/snippets/by-permission")
    fun getSnippetsByPermission(
        @RequestParam userId: String,
        @RequestParam permission: String,
    ): ResponseEntity<List<String>> {
        log.info(
            "GET /snippets/by-permission - Fetching snippets for user $userId with permission $permission",
        )
        val snippetIds =
            authorizationService.getSnippetsByPermission(
                userId,
                permission,
            )
        log.warn("GET /snippets/by-permission - Retrieved ${snippetIds.size} snippets")
        return ResponseEntity.ok(snippetIds)
    }
}
