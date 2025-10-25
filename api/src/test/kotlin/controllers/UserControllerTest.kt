package controllers

import dtos.CreateUserRequestDTO
import dtos.UserResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import services.MockUserService

class UserControllerTest {

    private lateinit var userService: MockUserService
    private lateinit var controller: UserController

    @BeforeEach
    fun setUp() {
        userService = MockUserService()
        controller = UserController(userService)
    }

    @Test
    fun `createUser should return 200 with user details`() {
        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "SecurePassword123!",
                name = "Test User",
            )

        val response = controller.createUser(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals("test@example.com", response.body!!.email)
        assertEquals("Test User", response.body!!.name)
        assertNotNull(response.body!!.userId)
        assertTrue(userService.createUserCalled)
        assertEquals(request, userService.lastCreatedUser)
    }

    @Test
    fun `createUser should handle different email formats`() {
        val emails =
            listOf(
                "user@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "123@example.com",
            )

        emails.forEach { email ->
            userService.reset()
            val request =
                CreateUserRequestDTO(
                    email = email,
                    password = "Password123!",
                    name = "User Name",
                )

            val response = controller.createUser(request)

            assertEquals(200, response.statusCode.value())
            assertNotNull(response.body)
            assertEquals(email, response.body!!.email)
        }
    }

    @Test
    fun `createUser should propagate service exceptions`() {
        userService.shouldThrowOnCreate = true

        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "Password123!",
                name = "Test User",
            )

        assertThrows<RuntimeException> {
            controller.createUser(request)
        }
    }

    @Test
    fun `deleteUser should return 204 on success`() {
        val userId = "auth0|123456"
        userService.addUser(
            UserResponseDTO(
                email = "test@example.com",
                name = "Test User",
                userId = userId,
            ),
        )

        val response = controller.deleteUser(userId)

        assertEquals(204, response.statusCode.value())
        assertTrue(userService.deleteUserCalled)
        assertEquals(userId, userService.lastDeletedUserId)
    }

    @Test
    fun `deleteUser should throw exception when user not found`() {
        val userId = "nonexistent|123"

        assertThrows<IllegalArgumentException> {
            controller.deleteUser(userId)
        }
    }

    @Test
    fun `deleteUser should propagate service exceptions`() {
        val userId = "auth0|123456"
        userService.addUser(
            UserResponseDTO(
                email = "test@example.com",
                name = "Test User",
                userId = userId,
            ),
        )
        userService.shouldThrowOnDelete = true

        assertThrows<RuntimeException> {
            controller.deleteUser(userId)
        }
    }

    @Test
    fun `createUser should handle special characters in name`() {
        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "Password123!",
                name = "Tést Üser-Ñame",
            )

        val response = controller.createUser(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals("Tést Üser-Ñame", response.body!!.name)
    }

    @Test
    fun `createUser should handle long names`() {
        val longName = "A".repeat(100)
        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "Password123!",
                name = longName,
            )

        val response = controller.createUser(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(longName, response.body!!.name)
    }

    @Test
    fun `deleteUser should handle different userId formats`() {
        val userIds =
            listOf(
                "auth0|123456",
                "auth0|abc-def-ghi",
                "google-oauth2|123456789",
                "github|username",
            )

        userIds.forEach { userId ->
            userService.reset()
            userService.addUser(
                UserResponseDTO(
                    email = "test@example.com",
                    name = "Test User",
                    userId = userId,
                ),
            )

            val response = controller.deleteUser(userId)

            assertEquals(204, response.statusCode.value())
            assertEquals(userId, userService.lastDeletedUserId)
        }
    }

    @Test
    fun `createUser should create multiple users successfully`() {
        val users =
            listOf(
                CreateUserRequestDTO("user1@example.com", "Pass123!", "User One"),
                CreateUserRequestDTO("user2@example.com", "Pass123!", "User Two"),
                CreateUserRequestDTO("user3@example.com", "Pass123!", "User Three"),
            )

        users.forEach { request ->
            val response = controller.createUser(request)

            assertEquals(200, response.statusCode.value())
            assertNotNull(response.body)
            assertEquals(request.email, response.body!!.email)
            assertEquals(request.name, response.body!!.name)
        }
    }
}
