package controllers

import api.dtos.GetSnippetPermissionsRequestDTO
import dtos.CheckPermissionRequestDTO
import dtos.CheckPermissionResponseDTO
import dtos.GrantPermissionRequestDTO
import dtos.RevokePermissionRequestDTO
import dtos.PermissionResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import services.AuthorizationService

@RestController
@RequestMapping("/api/authorization")
class AuthorizationController(
    private val authorizationService: AuthorizationService,
) {

    @PostMapping("/check")
    fun checkPermission(
        @RequestBody request: CheckPermissionRequestDTO,
    ): ResponseEntity<CheckPermissionResponseDTO> {
        val allowed = authorizationService.checkPermission(request)
        return ResponseEntity.ok(CheckPermissionResponseDTO(allowed))
    }

    @PostMapping("/permissions")
    fun grantPermission(
        @RequestBody request: GrantPermissionRequestDTO,
    ): ResponseEntity<PermissionResponseDTO> {
        val permission = authorizationService.grantPermission(request)
        return ResponseEntity.ok(permission)
    }

    @PostMapping("/permissions/revoke")
    fun revokePermission(
        @RequestBody request: RevokePermissionRequestDTO,
    ): ResponseEntity<Void> {
        authorizationService.revokePermission(
            request.userId,
            request.snippetId,
            request.requesterId,
        )
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/permissions/snippet")
    fun getSnippetPermissions(
        @RequestBody request: GetSnippetPermissionsRequestDTO,
    ): ResponseEntity<List<PermissionResponseDTO>> {
        val permissions =
            authorizationService.getSnippetPermissions(
                request.snippetId,
                request.requesterId,
            )
        return ResponseEntity.ok(permissions)
    }

    @GetMapping("/permissions/user/{userId}")
    fun getUserPermissions(
        @PathVariable userId: String,
    ): ResponseEntity<List<PermissionResponseDTO>> {
        val permissions = authorizationService.getUserPermissions(userId)
        return ResponseEntity.ok(permissions)
    }

    @GetMapping("/snippets/by-permission")
    fun getSnippetsByPermission(
        @RequestParam userId: String,
        @RequestParam permission: String,
    ): ResponseEntity<List<String>> {
        val snippetIds =
            authorizationService.getSnippetsByPermission(
                userId,
                permission,
            )
        return ResponseEntity.ok(snippetIds)
    }
}
