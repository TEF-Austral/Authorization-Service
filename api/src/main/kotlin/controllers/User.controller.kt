package controllers

import dtos.CreateUserRequestDTO
import dtos.UserResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import services.Auth0UserService

@RestController
@RequestMapping("/users")
class UserController(
    private val auth0UserService: Auth0UserService,
) {
    @PostMapping
    fun createUser(
        @RequestBody request: CreateUserRequestDTO,
    ): ResponseEntity<UserResponseDTO> {
        val user = auth0UserService.createUser(request)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(
        @PathVariable userId: String,
    ): ResponseEntity<Void> {
        auth0UserService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}
