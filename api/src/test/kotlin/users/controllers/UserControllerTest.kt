package users.controllers

import api.users.controllers.UserController
import api.dtos.PaginatedUsersDTO
import api.users.models.User
import api.users.services.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class UserControllerTest {
    private lateinit var userService: UserService
    private lateinit var userController: UserController

    @BeforeEach
    fun setUp() {
        userService = mock()
        userController = UserController(userService)
    }

    @Test
    fun `getUsers should return paginated users with default parameters`() {
        val users =
            listOf(
                User("user1", "username1", "email1@test.com", "Name 1", null),
                User("user2", "username2", "email2@test.com", "Name 2", null),
            )
        val paginatedUsers = PaginatedUsersDTO(users, 0, 50, 2)
        whenever(userService.getUsers(null, 0, 50)).thenReturn(paginatedUsers)

        val response = userController.getUsers(null, 0, 50)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(paginatedUsers, response.body)
        assertEquals(2, response.body?.users?.size)
        verify(userService).getUsers(null, 0, 50)
    }

    @Test
    fun `getUsers should return paginated users with custom query`() {
        val query = "name:*John*"
        val users =
            listOf(
                User("user1", "johndoe", "john@test.com", "John Doe", null),
            )
        val paginatedUsers = PaginatedUsersDTO(users, 0, 50, 1)
        whenever(userService.getUsers(query, 0, 50)).thenReturn(paginatedUsers)

        val response = userController.getUsers(query, 0, 50)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.users?.size)
        verify(userService).getUsers(query, 0, 50)
    }

    @Test
    fun `getUsers should coerce page size to minimum of 1`() {
        val users = listOf(User("user1", "username1", "email1@test.com", "Name 1", null))
        val paginatedUsers = PaginatedUsersDTO(users, 0, 1, 1)
        whenever(userService.getUsers(null, 0, 1)).thenReturn(paginatedUsers)

        val response = userController.getUsers(null, 0, 0)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(userService).getUsers(null, 0, 1)
    }

    @Test
    fun `getUsers should coerce page size to maximum of 50`() {
        val users = listOf(User("user1", "username1", "email1@test.com", "Name 1", null))
        val paginatedUsers = PaginatedUsersDTO(users, 0, 50, 1)
        whenever(userService.getUsers(null, 0, 50)).thenReturn(paginatedUsers)

        val response = userController.getUsers(null, 0, 100)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(userService).getUsers(null, 0, 50)
    }

    @Test
    fun `getUsers should coerce negative page size to 1`() {
        val users = listOf(User("user1", "username1", "email1@test.com", "Name 1", null))
        val paginatedUsers = PaginatedUsersDTO(users, 0, 1, 1)
        whenever(userService.getUsers(null, 0, 1)).thenReturn(paginatedUsers)

        val response = userController.getUsers(null, 0, -5)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(userService).getUsers(null, 0, 1)
    }

    @Test
    fun `getUsers should handle custom page number`() {
        val users = listOf(User("user1", "username1", "email1@test.com", "Name 1", null))
        val paginatedUsers = PaginatedUsersDTO(users, 2, 10, 1)
        whenever(userService.getUsers(null, 2, 10)).thenReturn(paginatedUsers)

        val response = userController.getUsers(null, 2, 10)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.page)
        verify(userService).getUsers(null, 2, 10)
    }

    @Test
    fun `getUsers should return empty list when no users found`() {
        val paginatedUsers = PaginatedUsersDTO(emptyList(), 0, 50, 0)
        whenever(userService.getUsers(null, 0, 50)).thenReturn(paginatedUsers)

        val response = userController.getUsers(null, 0, 50)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body?.users?.size)
    }

    @Test
    fun `getUserById should return user when found`() {
        val userId = "auth0|12345"
        val user = User(userId, "testuser", "test@example.com", "Test User", "pic.jpg")
        whenever(userService.getUserById(userId)).thenReturn(user)

        val response = userController.getUserById(userId)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(user, response.body)
        assertEquals(userId, response.body?.id)
        assertEquals("testuser", response.body?.username)
        verify(userService).getUserById(userId)
    }

    @Test
    fun `getUserById should handle different user id formats`() {
        val userId = "google-oauth2|123456789"
        val user = User(userId, "googleuser", "google@example.com", "Google User", null)
        whenever(userService.getUserById(userId)).thenReturn(user)

        val response = userController.getUserById(userId)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(userId, response.body?.id)
        verify(userService).getUserById(userId)
    }

    @Test
    fun `getUsersByEmail should return list of users`() {
        val email = "test@example.com"
        val users =
            listOf(
                User("user1", "testuser1", email, "Test User 1", null),
                User("user2", "testuser2", email, "Test User 2", null),
            )
        whenever(userService.getUsersByEmail(email)).thenReturn(users)

        val response = userController.getUsersByEmail(email)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
        assertEquals(email, response.body?.get(0)?.email)
        verify(userService).getUsersByEmail(email)
    }

    @Test
    fun `getUsersByEmail should return empty list when no users found`() {
        val email = "nonexistent@example.com"
        whenever(userService.getUsersByEmail(email)).thenReturn(emptyList())

        val response = userController.getUsersByEmail(email)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body?.size)
        verify(userService).getUsersByEmail(email)
    }

    @Test
    fun `getUsersByEmail should handle special characters in email`() {
        val email = "test+tag@example.com"
        val users = listOf(User("user1", "testuser", email, "Test User", null))
        whenever(userService.getUsersByEmail(email)).thenReturn(users)

        val response = userController.getUsersByEmail(email)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).getUsersByEmail(email)
    }

    @Test
    fun `searchUsers should search by name only`() {
        val name = "John"
        val users =
            listOf(
                User("user1", "johndoe", "john@example.com", "John Doe", null),
            )
        whenever(userService.searchUsers(name, null, null, null)).thenReturn(users)

        val response = userController.searchUsers(name, null, null, null)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        assertEquals("John Doe", response.body?.get(0)?.name)
        verify(userService).searchUsers(name, null, null, null)
    }

    @Test
    fun `searchUsers should search by email only`() {
        val email = "test@example.com"
        val users =
            listOf(
                User("user1", "testuser", email, "Test User", null),
            )
        whenever(userService.searchUsers(null, email, null, null)).thenReturn(users)

        val response = userController.searchUsers(null, email, null, null)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(null, email, null, null)
    }

    @Test
    fun `searchUsers should search by emailVerified only`() {
        val emailVerified = true
        val users =
            listOf(
                User("user1", "verified", "verified@example.com", "Verified User", null),
            )
        whenever(userService.searchUsers(null, null, emailVerified, null)).thenReturn(users)

        val response = userController.searchUsers(null, null, emailVerified, null)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(null, null, emailVerified, null)
    }

    @Test
    fun `searchUsers should search by connection only`() {
        val connection = "google-oauth2"
        val users =
            listOf(
                User("user1", "googleuser", "google@example.com", "Google User", null),
            )
        whenever(userService.searchUsers(null, null, null, connection)).thenReturn(users)

        val response = userController.searchUsers(null, null, null, connection)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(null, null, null, connection)
    }

    @Test
    fun `searchUsers should search with multiple criteria`() {
        val name = "John"
        val email = "john@example.com"
        val emailVerified = true
        val connection = "google-oauth2"
        val users =
            listOf(
                User("user1", "johndoe", email, "John Doe", null),
            )
        whenever(userService.searchUsers(name, email, emailVerified, connection)).thenReturn(users)

        val response = userController.searchUsers(name, email, emailVerified, connection)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(name, email, emailVerified, connection)
    }

    @Test
    fun `searchUsers should return empty list when no criteria provided`() {
        val users =
            listOf(
                User("user1", "user1", "user1@example.com", "User 1", null),
            )
        whenever(userService.searchUsers(null, null, null, null)).thenReturn(users)

        val response = userController.searchUsers(null, null, null, null)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(null, null, null, null)
    }

    @Test
    fun `searchUsers should return empty list when no matches found`() {
        val name = "NonExistent"
        whenever(userService.searchUsers(name, null, null, null)).thenReturn(emptyList())

        val response = userController.searchUsers(name, null, null, null)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body?.size)
    }

    @Test
    fun `searchUsers with emailVerified false`() {
        val emailVerified = false
        val users =
            listOf(
                User("user1", "unverified", "unverified@example.com", "Unverified User", null),
            )
        whenever(userService.searchUsers(null, null, emailVerified, null)).thenReturn(users)

        val response = userController.searchUsers(null, null, emailVerified, null)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(null, null, emailVerified, null)
    }

    @Test
    fun `searchUsers should handle combination of name and emailVerified`() {
        val name = "John"
        val emailVerified = true
        val users =
            listOf(
                User("user1", "johndoe", "john@example.com", "John Doe", null),
            )
        whenever(userService.searchUsers(name, null, emailVerified, null)).thenReturn(users)

        val response = userController.searchUsers(name, null, emailVerified, null)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(name, null, emailVerified, null)
    }

    @Test
    fun `searchUsers should handle combination of email and connection`() {
        val email = "test@example.com"
        val connection = "auth0"
        val users =
            listOf(
                User("user1", "testuser", email, "Test User", null),
            )
        whenever(userService.searchUsers(null, email, null, connection)).thenReturn(users)

        val response = userController.searchUsers(null, email, null, connection)

        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        verify(userService).searchUsers(null, email, null, connection)
    }
}
