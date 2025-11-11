package api.users.controllers

import api.dtos.PaginatedUsersDTO
import api.users.models.User
import api.users.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(UserController::class.java)

    @GetMapping
    fun getUsers(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") pageSize: Int,
    ): ResponseEntity<PaginatedUsersDTO> {
        log.info(
            "GET /api/users - Fetching users with query: $query, page: $page, pageSize: $pageSize",
        )
        val validatedPageSize = pageSize.coerceIn(1, 50)
        val users = userService.getUsers(query, page, validatedPageSize)
        log.warn("GET /api/users - Retrieved ${users.users.size} users")
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{userId}")
    fun getUserById(
        @PathVariable userId: String,
    ): ResponseEntity<User> {
        log.info("GET /api/users/$userId - Fetching user by ID")
        val user = userService.getUserById(userId)
        log.warn("GET /api/users/$userId - User retrieved successfully")
        return ResponseEntity.ok(user)
    }

    @GetMapping("/by-email")
    fun getUsersByEmail(
        @RequestParam email: String,
    ): ResponseEntity<List<User>> {
        log.info("GET /api/users/by-email - Fetching users by email: $email")
        val users = userService.getUsersByEmail(email)
        log.warn("GET /api/users/by-email - Retrieved ${users.size} users")
        return ResponseEntity.ok(users)
    }

    @GetMapping("/search")
    fun searchUsers(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) emailVerified: Boolean?,
        @RequestParam(required = false) connection: String?,
    ): ResponseEntity<List<User>> {
        log.info(
            "GET /api/users/search - Searching users with name: $name, email: $email, emailVerified: $emailVerified, connection: $connection",
        )
        val users = userService.searchUsers(name, email, emailVerified, connection)
        log.warn("GET /api/users/search - Found ${users.size} users")
        return ResponseEntity.ok(users)
    }
}
