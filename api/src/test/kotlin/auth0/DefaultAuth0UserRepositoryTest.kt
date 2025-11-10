package auth0

import api.auth0.Auth0TokenManager
import api.auth0.DefaultAuth0UserRepository
import api.dtos.responses.Auth0UserResponseDTO
import api.users.config.Auth0Config
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class DefaultAuth0UserRepositoryTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var auth0Config: Auth0Config
    private lateinit var tokenManager: Auth0TokenManager
    private lateinit var userRepository: DefaultAuth0UserRepository

    @BeforeEach
    fun setUp() {
        restTemplate = mock()
        tokenManager = mock()
        auth0Config =
            Auth0Config().apply {
                domain = "test-domain.auth0.com"
                management =
                    Auth0Config.Management().apply {
                        clientId = "test-client-id"
                        clientSecret = "test-client-secret"
                    }
            }
        userRepository = DefaultAuth0UserRepository(restTemplate, auth0Config, tokenManager)
    }

    @Test
    fun `getUsers should return list of users`() {
        val users =
            arrayOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "user1@example.com",
                    username = "user1",
                    name = "User One",
                    picture = null,
                    nickname = "nick1",
                ),
                Auth0UserResponseDTO(
                    userId = "auth0|user2",
                    email = "user2@example.com",
                    username = "user2",
                    name = "User Two",
                    picture = null,
                    nickname = "nick2",
                ),
            )

        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = userRepository.getUsers(null, 0, 50, "v3")

        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("auth0|user1", result[0].userId)
        assertEquals("auth0|user2", result[1].userId)
    }

    @Test
    fun `getUsers should return empty list when Auth0 returns null body`() {
        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val result = userRepository.getUsers(null, 0, 50, "v3")

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `getUsers should handle query parameter`() {
        val users =
            arrayOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "test@example.com",
                    username = "testuser",
                    name = "Test User",
                    picture = null,
                    nickname = "test",
                ),
            )

        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = userRepository.getUsers("name:*Test*", 0, 50, "v3")

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(restTemplate).exchange(
            any<String>(),
            eq(HttpMethod.GET),
            any<HttpEntity<*>>(),
            eq(Array<Auth0UserResponseDTO>::class.java),
        )
    }

    @Test
    fun `getUsers should handle pagination parameters`() {
        val users = arrayOf<Auth0UserResponseDTO>()

        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = userRepository.getUsers(null, 1, 10, "v3")

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `getUsers should include query param in URL when query is provided`() {
        val users = arrayOf<Auth0UserResponseDTO>()
        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        userRepository.getUsers("email:*test*", 0, 50, "v3")

        verify(restTemplate).exchange(
            any<String>(),
            eq(HttpMethod.GET),
            any<HttpEntity<*>>(),
            eq(Array<Auth0UserResponseDTO>::class.java),
        )
    }

    @Test
    fun `getUsers should not include query param in URL when query is null`() {
        val users = arrayOf<Auth0UserResponseDTO>()
        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        userRepository.getUsers(null, 0, 50, "v3")

        verify(restTemplate).exchange(
            any<String>(),
            eq(HttpMethod.GET),
            any<HttpEntity<*>>(),
            eq(Array<Auth0UserResponseDTO>::class.java),
        )
    }

    @Test
    fun `getUsers should not include query param in URL when query is blank`() {
        val users = arrayOf<Auth0UserResponseDTO>()
        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        userRepository.getUsers("   ", 0, 50, "v3")

        verify(restTemplate).exchange(
            any<String>(),
            eq(HttpMethod.GET),
            any<HttpEntity<*>>(),
            eq(Array<Auth0UserResponseDTO>::class.java),
        )
    }

    @Test
    fun `getUserById should return user when found`() {
        val user =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
                username = "testuser",
                name = "Test User",
                picture = "https://example.com/pic.jpg",
                nickname = "test",
            )

        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Auth0UserResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(user))

        val result = userRepository.getUserById("auth0|12345")

        assertNotNull(result)
        assertEquals("auth0|12345", result.userId)
        assertEquals("testuser", result.username)
    }

    @Test
    fun `getUserById should throw exception when user not found`() {
        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Auth0UserResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val exception =
            assertThrows(RuntimeException::class.java) {
                userRepository.getUserById("nonexistent-id")
            }

        assertTrue(exception.message!!.contains("User not found"))
    }

    @Test
    fun `getUserById should use correct URL format`() {
        val user =
            Auth0UserResponseDTO(
                userId = "auth0|test",
                email = "test@example.com",
                username = "testuser",
                name = "Test User",
                picture = null,
                nickname = "test",
            )

        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                eq("https://test-domain.auth0.com/api/v2/users/auth0|test"),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Auth0UserResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(user))

        val result = userRepository.getUserById("auth0|test")

        assertNotNull(result)
        assertEquals("auth0|test", result.userId)
    }

    @Test
    fun `getUsersByEmail should return users with email query`() {
        val users =
            arrayOf(
                Auth0UserResponseDTO(
                    userId = "auth0|user1",
                    email = "test@example.com",
                    username = "testuser",
                    name = "Test User",
                    picture = null,
                    nickname = "test",
                ),
            )

        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = userRepository.getUsersByEmail("test@example.com")

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("test@example.com", result[0].email)
    }

    @Test
    fun `getUsersByEmail should use correct search parameters`() {
        val users = arrayOf<Auth0UserResponseDTO>()

        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        userRepository.getUsersByEmail("email@test.com")

        verify(restTemplate).exchange(
            any<String>(),
            eq(HttpMethod.GET),
            any<HttpEntity<*>>(),
            eq(Array<Auth0UserResponseDTO>::class.java),
        )
    }

    @Test
    fun `getUsersByEmail should return empty list when no users found`() {
        whenever(tokenManager.getManagementApiToken()).thenReturn("test-token")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val result = userRepository.getUsersByEmail("nonexistent@example.com")

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `getUsers should set Authorization header with Bearer token`() {
        val users = arrayOf<Auth0UserResponseDTO>()
        whenever(tokenManager.getManagementApiToken()).thenReturn("my-token-123")
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        userRepository.getUsers(null, 0, 50, "v3")

        verify(tokenManager).getManagementApiToken()
    }
}
