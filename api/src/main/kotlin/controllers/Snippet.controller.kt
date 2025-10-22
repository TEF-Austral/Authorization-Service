package api.controllers

import api.dtos.ShareRequestDTO
import api.entities.PermissionType
import api.entities.Snippet
import api.entities.UserPermission
import api.repositories.UserPermissionRepository
import api.repositories.SnippetRepository
import api.services.SnippetAuthorizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val authService: SnippetAuthorizationService, // El servicio ABAC
    private val permissionRepo: UserPermissionRepository, // Para 'share'
    private val snippetRepo: SnippetRepository, // Simulación de servicio de negocio
) {
    /**
     * ACCIÓN: Leer
     * POLÍTICA: Requiere permiso 'READ'
     */
    @GetMapping("/{id}")
    fun getSnippet(
        @PathVariable id: Long,
        auth: Authentication,
    ): ResponseEntity<Snippet> {
        val subject = authService.getSubjectFromJwt(auth.principal as Jwt)

        // Aquí ocurre la magia de ABAC
        if (!authService.checkPermission(subject, id, PermissionType.READ)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val snippet =
            snippetRepo.findById(id).orElse(null)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(snippet)
    }

    /**
     * ACCIÓN: Borrar
     * POLÍTICA: Requiere permiso 'DELETE'
     */
    @DeleteMapping("/{id}")
    fun deleteSnippet(
        @PathVariable id: Long,
        auth: Authentication,
    ): ResponseEntity<Void> {
        val subject = authService.getSubjectFromJwt(auth.principal as Jwt)

        if (!authService.checkPermission(subject, id, PermissionType.DELETE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        // Aquí también deberías borrar todos los permisos asociados
        // (snippetRepo.deleteById(id) no lo hará automáticamente sin un 'cascade')

        snippetRepo.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * ACCIÓN: Correr Tests
     * POLÍTICA: Requiere permiso 'RUN_TESTS'
     */
    @PostMapping("/{id}/run-tests")
    fun runTests(
        @PathVariable id: Long,
        auth: Authentication,
    ): ResponseEntity<String> {
        val subject = authService.getSubjectFromJwt(auth.principal as Jwt)

        if (!authService.checkPermission(subject, id, PermissionType.RUN_TESTS)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val results = "Tests para Snippet $id ejecutados OK."
        return ResponseEntity.ok(results)
    }

    /**
     * ACCIÓN: Compartir (Especial: Concede permisos a otros)
     * POLÍTICA: Requiere permiso 'SHARE'
     */
    @PostMapping("/{id}/share")
    fun shareSnippet(
        @PathVariable id: Long,
        @RequestBody shareRequest: ShareRequestDTO,
        auth: Authentication,
    ): ResponseEntity<Any> {
        val subject = authService.getSubjectFromJwt(auth.principal as Jwt)

        // 1. Política ABAC: ¿Puede el usuario actual 'compartir' este snippet?
        if (!authService.checkPermission(subject, id, PermissionType.SHARE)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("No tienes permiso para 'compartir' este snippet.")
        }

        // 2. Autorizado: Crear el nuevo permiso para el otro usuario

        val newPermission =
            UserPermission(
                userId = shareRequest.userIdToShareWith,
                snippetId = id,
                permission = shareRequest.permissionToGrant,
            )
        permissionRepo.save(newPermission)

        return ResponseEntity.status(HttpStatus.CREATED).body(newPermission)
    }
}
