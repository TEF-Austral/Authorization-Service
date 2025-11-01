package api.users.controllers

import api.users.dtos.PaginatedUsersDTO
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

    /**
     * Get paginated list of users from Auth0
     *
     * @param query Optional search query (Auth0 query syntax)
     * @param page Page number (default: 0)
     * @param pageSize Number of results per page (default: 50, max: 50)
     * @return Paginated list of users
     */
    @GetMapping
    fun getUsers(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") pageSize: Int,
    ): ResponseEntity<PaginatedUsersDTO> {
        val validatedPageSize = pageSize.coerceIn(1, 50)
        val users = userService.getUsers(query, page, validatedPageSize)
        return ResponseEntity.ok(users)
    }

    /**
     * Get a specific user by ID
     *
     * @param userId The Auth0 user ID
     * @return User details
     */
    @GetMapping("/{userId}")
    fun getUserById(
        @PathVariable userId: String,
    ): ResponseEntity<User> {
        val user = userService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    /**
     * Search users by email
     *
     * @param email Email address to search for
     * @return List of users matching the email
     */
    @GetMapping("/by-email")
    fun getUsersByEmail(
        @RequestParam email: String,
    ): ResponseEntity<List<User>> {
        val users = userService.getUsersByEmail(email)
        return ResponseEntity.ok(users)
    }

    /**
     * Search users with various filters
     *
     * @param name Name to search for (partial match)
     * @param email Email to search for (exact match)
     * @param emailVerified Filter by email verification status
     * @param connection Filter by identity connection
     * @return List of users matching the criteria
     */
    @GetMapping("/search")
    fun searchUsers(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) emailVerified: Boolean?,
        @RequestParam(required = false) connection: String?,
    ): ResponseEntity<List<User>> {
        val users = userService.searchUsers(name, email, emailVerified, connection)
        return ResponseEntity.ok(users)
    }
}
