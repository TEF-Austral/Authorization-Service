package api.controllers

import api.dtos.AuthorizationCheckResponseDTO
import api.entities.PermissionType
import api.repositories.UserPermissionRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permissions")
class PermissionController(
    private val permissionRepository: UserPermissionRepository,
) {
    @GetMapping("/check")
    fun checkPermission(
        @RequestParam("user_id") userId: String,
        @RequestParam("snippet_id") snippetId: Long,
        @RequestParam("action") action: PermissionType,
    ): ResponseEntity<AuthorizationCheckResponseDTO> {
        val hasPermission =
            permissionRepository.existsByUserIdAndSnippetIdAndPermission(
                userId = userId,
                snippetId = snippetId,
                permission = action,
            )

        return ResponseEntity.ok(AuthorizationCheckResponseDTO(allowed = hasPermission))
    }

// NOTA: Los endpoints para 'crear' permisos (como en la acción de 'share')// vivirán en el 'execution-service', ya que es una acción de negocio// sobre un snippet. Este servicio solo 'lee' permisos.
}
