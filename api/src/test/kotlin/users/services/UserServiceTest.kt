package users.services

import api.users.dtos.Auth0UserResponseDTO
import api.users.services.Auth0ClientService
import api.users.services.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserServiceTest {
    private lateinit var auth0ClientService: Auth0ClientService
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        auth0ClientService = mock()
        userService = UserService(auth0ClientService)
    }

    @Test
    fun `getUsers should return paginated users with default parameters`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "user1@example.com",
                    username = "user1",
                    name = "User One",
                    picture = "https://example.com/pic1.jpg",
                    nickname = "user1nick",
                ),
                Auth0UserResponseDTO(
                    userId = "auth0|user2",
                    email = "user2@example.com",
                    username = "user2",
                    name = "User Two",
                    picture = "https://example.com/pic2.jpg",
                    nickname = "user2nick",
                ),
            )
        whenever(auth0ClientService.getUsers(null, 0, 50)).thenReturn(auth0Users)

        val result = userService.getUsers()

        assertNotNull(result)
        assertEquals(2, result.users.size)
        assertEquals(0, result.page)
        assertEquals(50, result.pageSize)
        assertEquals(2, result.total)
        assertEquals("auth0|user1", result.users[0].id)
        assertEquals("user1", result.users[0].username)
        assertEquals("user1@example.com", result.users[0].email)
        verify(auth0ClientService).getUsers(null, 0, 50)
    }

    @Test
    fun `getUsers should return paginated users with custom parameters`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "user1@example.com",
                    username = "user1",
                    name = "User One",
                    picture = null,
                    nickname = "user1nick",
                ),
            )
        whenever(auth0ClientService.getUsers("name:*test*", 1, 10)).thenReturn(auth0Users)

        val result = userService.getUsers(query = "name:*test*", page = 1, pageSize = 10)

        assertNotNull(result)
        assertEquals(1, result.users.size)
        assertEquals(1, result.page)
        assertEquals(10, result.pageSize)
        assertEquals(1, result.total)
        verify(auth0ClientService).getUsers("name:*test*", 1, 10)
    }

    @Test
    fun `getUsers should return empty list when no users found`() {
        whenever(auth0ClientService.getUsers(null, 0, 50)).thenReturn(emptyList())

        val result = userService.getUsers()

        assertNotNull(result)
        assertEquals(0, result.users.size)
        assertEquals(0, result.page)
        assertEquals(50, result.pageSize)
        assertEquals(0, result.total)
    }

    @Test
    fun `getUserById should return user when found`() {
        val userId = "auth0|12345"
        val auth0User =
            Auth0UserResponseDTO(
                userId = userId,
                email = "test@example.com",
                username = "testuser",
                name = "Test User",
                picture = "https://example.com/pic.jpg",
                nickname = "testnick",
            )
        whenever(auth0ClientService.getUserById(userId)).thenReturn(auth0User)

        val result = userService.getUserById(userId)

        assertNotNull(result)
        assertEquals(userId, result.id)
        assertEquals("testuser", result.username)
        assertEquals("test@example.com", result.email)
        assertEquals("Test User", result.name)
        assertEquals("https://example.com/pic.jpg", result.picture)
        verify(auth0ClientService).getUserById(userId)
    }

    @Test
    fun `getUserById should use nickname when username is null`() {
        val userId = "auth0|12345"
        val auth0User =
            Auth0UserResponseDTO(
                userId = userId,
                email = "test@example.com",
                username = null,
                name = "Test User",
                picture = "https://example.com/pic.jpg",
                nickname = "testnick",
            )
        whenever(auth0ClientService.getUserById(userId)).thenReturn(auth0User)

        val result = userService.getUserById(userId)

        assertNotNull(result)
        assertEquals("testnick", result.username)
        verify(auth0ClientService).getUserById(userId)
    }

    @Test
    fun `getUsersByEmail should return list of users`() {
        val email = "test@example.com"
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = email,
                    username = "testuser1",
                    name = "Test User 1",
                    picture = null,
                    nickname = "nick1",
                ),
                Auth0UserResponseDTO(
                    userId = "auth0|user2",
                    email = email,
                    username = "testuser2",
                    name = "Test User 2",
                    picture = null,
                    nickname = "nick2",
                ),
            )
        whenever(auth0ClientService.getUsersByEmail(email)).thenReturn(auth0Users)

        val result = userService.getUsersByEmail(email)

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("auth0|user1", result[0].id)
        assertEquals("auth0|user2", result[1].id)
        assertEquals(email, result[0].email)
        verify(auth0ClientService).getUsersByEmail(email)
    }

    @Test
    fun `getUsersByEmail should return empty list when no users found`() {
        val email = "nonexistent@example.com"
        whenever(auth0ClientService.getUsersByEmail(email)).thenReturn(emptyList())

        val result = userService.getUsersByEmail(email)

        assertNotNull(result)
        assertEquals(0, result.size)
        verify(auth0ClientService).getUsersByEmail(email)
    }

    @Test
    fun `searchUsers should search by name only`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "john@example.com",
                    username = "johndoe",
                    name = "John Doe",
                    picture = null,
                    nickname = "john",
                ),
            )
        whenever(auth0ClientService.getUsers(query = "name:*John*")).thenReturn(auth0Users)

        val result = userService.searchUsers(name = "John")

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("John Doe", result[0].name)
        verify(auth0ClientService).getUsers(query = "name:*John*")
    }

    @Test
    fun `searchUsers should search by email only`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "test@example.com",
                    username = "testuser",
                    name = "Test User",
                    picture = null,
                    nickname = "test",
                ),
            )
        whenever(
            auth0ClientService.getUsers(query = "email:\"test@example.com\""),
        ).thenReturn(auth0Users)

        val result = userService.searchUsers(email = "test@example.com")

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("test@example.com", result[0].email)
        verify(auth0ClientService).getUsers(query = "email:\"test@example.com\"")
    }

    @Test
    fun `searchUsers should search by emailVerified only`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "verified@example.com",
                    username = "verifieduser",
                    name = "Verified User",
                    picture = null,
                    nickname = "verified",
                ),
            )
        whenever(auth0ClientService.getUsers(query = "email_verified:true")).thenReturn(auth0Users)

        val result = userService.searchUsers(emailVerified = true)

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(auth0ClientService).getUsers(query = "email_verified:true")
    }

    @Test
    fun `searchUsers should search by connection only`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "user@example.com",
                    username = "user",
                    name = "User",
                    picture = null,
                    nickname = "user",
                ),
            )
        whenever(
            auth0ClientService.getUsers(query = "identities.connection:\"google-oauth2\""),
        ).thenReturn(auth0Users)

        val result = userService.searchUsers(connection = "google-oauth2")

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(auth0ClientService).getUsers(query = "identities.connection:\"google-oauth2\"")
    }

    @Test
    fun `searchUsers should call getUsers with null query when no criteria provided`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "user@example.com",
                    username = "user",
                    name = "User",
                    picture = null,
                    nickname = "user",
                ),
            )
        whenever(auth0ClientService.getUsers(query = null)).thenReturn(auth0Users)

        val result = userService.searchUsers()

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(auth0ClientService).getUsers(query = null)
    }

    @Test
    fun `searchUsers should return empty list when no matches found`() {
        whenever(auth0ClientService.getUsers(query = "name:*NonExistent*")).thenReturn(emptyList())

        val result = userService.searchUsers(name = "NonExistent")

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `mapToUser should handle user with all fields populated`() {
        val auth0User =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
                username = "testuser",
                name = "Test User",
                picture = "https://example.com/pic.jpg",
                nickname = "testnick",
            )
        whenever(auth0ClientService.getUserById("auth0|12345")).thenReturn(auth0User)

        val result = userService.getUserById("auth0|12345")

        assertEquals("auth0|12345", result.id)
        assertEquals("testuser", result.username)
        assertEquals("test@example.com", result.email)
        assertEquals("Test User", result.name)
        assertEquals("https://example.com/pic.jpg", result.picture)
    }

    @Test
    fun `mapToUser should handle user with null optional fields`() {
        val auth0User =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = null,
                username = null,
                name = null,
                picture = null,
                nickname = null,
            )
        whenever(auth0ClientService.getUserById("auth0|12345")).thenReturn(auth0User)

        val result = userService.getUserById("auth0|12345")

        assertEquals("auth0|12345", result.id)
        assertEquals(null, result.username)
        assertEquals(null, result.email)
        assertEquals(null, result.name)
        assertEquals(null, result.picture)
    }

    @Test
    fun `mapToUser should handle empty userId`() {
        val auth0User =
            Auth0UserResponseDTO(
                userId = null,
                email = "test@example.com",
                username = "testuser",
                name = "Test User",
                picture = null,
                nickname = "nick",
            )
        whenever(auth0ClientService.getUserById("test-id")).thenReturn(auth0User)

        val result = userService.getUserById("test-id")

        assertEquals("", result.id)
        assertEquals("testuser", result.username)
    }

    @Test
    fun `searchUsers with emailVerified false should use correct query`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "unverified@example.com",
                    username = "unverifieduser",
                    name = "Unverified User",
                    picture = null,
                    nickname = "unverified",
                ),
            )
        whenever(auth0ClientService.getUsers(query = "email_verified:false")).thenReturn(auth0Users)

        val result = userService.searchUsers(emailVerified = false)

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(auth0ClientService).getUsers(query = "email_verified:false")
    }

    @Test
    fun `searchUsers with multiple criteria should combine with AND`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "john@example.com",
                    username = "johndoe",
                    name = "John Doe",
                    picture = null,
                    nickname = "john",
                ),
            )
        whenever(
            auth0ClientService.getUsers(query = "name:*John* AND email:\"john@example.com\""),
        ).thenReturn(auth0Users)

        val result = userService.searchUsers(name = "John", email = "john@example.com")

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("John Doe", result[0].name)
        verify(auth0ClientService).getUsers(query = "name:*John* AND email:\"john@example.com\"")
    }

    @Test
    fun `searchUsers with name and emailVerified should combine with AND`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "verified@example.com",
                    username = "verifieduser",
                    name = "Verified User",
                    picture = null,
                    nickname = "verified",
                ),
            )
        whenever(
            auth0ClientService.getUsers(query = "name:*Verified* AND email_verified:true"),
        ).thenReturn(auth0Users)

        val result = userService.searchUsers(name = "Verified", emailVerified = true)

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(auth0ClientService).getUsers(query = "name:*Verified* AND email_verified:true")
    }

    @Test
    fun `searchUsers with all criteria should combine with AND`() {
        val auth0Users =
            listOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "test@example.com",
                    username = "testuser",
                    name = "Test User",
                    picture = null,
                    nickname = "test",
                ),
            )
        whenever(
            auth0ClientService.getUsers(
                query = "name:*Test* AND email:\"test@example.com\" AND email_verified:true AND identities.connection:\"auth0\"",
            ),
        ).thenReturn(auth0Users)

        val result =
            userService.searchUsers(
                name = "Test",
                email = "test@example.com",
                emailVerified = true,
                connection = "auth0",
            )

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(auth0ClientService).getUsers(
            query = "name:*Test* AND email:\"test@example.com\" AND email_verified:true AND identities.connection:\"auth0\"",
        )
    }
}
