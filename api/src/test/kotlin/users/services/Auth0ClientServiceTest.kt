package users.services

import api.users.config.Auth0Config
import api.users.dtos.Auth0TokenResponseDTO
import api.users.dtos.Auth0UserResponseDTO
import api.users.services.Auth0ClientService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class Auth0ClientServiceTest {
    private lateinit var restTemplate: RestTemplate
    private lateinit var auth0Config: Auth0Config
    private lateinit var auth0ClientService: Auth0ClientService

    @BeforeEach
    fun setUp() {
        restTemplate = mock()
        auth0Config =
            Auth0Config().apply {
                domain = "test-domain.auth0.com"
                management =
                    Auth0Config.Management().apply {
                        clientId = "test-client-id"
                        clientSecret = "test-client-secret"
                    }
            }
        auth0ClientService = Auth0ClientService(restTemplate, auth0Config)
    }

    @Test
    fun `getManagementApiToken should return token from Auth0`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)

        val token = auth0ClientService.getManagementApiToken()

        assertNotNull(token)
        assertEquals("test-access-token", token)
        verify(
            restTemplate,
        ).postForObject(any<String>(), any<HttpEntity<*>>(), eq(Auth0TokenResponseDTO::class.java))
    }

    @Test
    fun `getManagementApiToken should return cached token when not expired`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)

        val token1 = auth0ClientService.getManagementApiToken()
        val token2 = auth0ClientService.getManagementApiToken()

        assertEquals(token1, token2)
        verify(
            restTemplate,
            times(1),
        ).postForObject(any<String>(), any<HttpEntity<*>>(), eq(Auth0TokenResponseDTO::class.java))
    }

    @Test
    fun `getManagementApiToken should throw exception when Auth0 returns null`() {
        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(null)

        assertThrows(RuntimeException::class.java) {
            auth0ClientService.getManagementApiToken()
        }
    }

    @Test
    fun `getUsers should return list of users`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
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
            )

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = auth0ClientService.getUsers()

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("auth0|user1", result[0].userId)
    }

    @Test
    fun `getUsers should return empty list when Auth0 returns null body`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val result = auth0ClientService.getUsers()

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `getUsers should handle query parameter`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
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

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = auth0ClientService.getUsers(query = "name:*Test*")

        assertNotNull(result)
        assertEquals(1, result.size)
    }

    @Test
    fun `getUsers should handle pagination parameters`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
        val users = arrayOf<Auth0UserResponseDTO>()

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = auth0ClientService.getUsers(page = 1, pageSize = 10)

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `getUserById should return user when found`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
        val user =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
                username = "testuser",
                name = "Test User",
                picture = "https://example.com/pic.jpg",
                nickname = "test",
            )

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Auth0UserResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(user))

        val result = auth0ClientService.getUserById("auth0|12345")

        assertNotNull(result)
        assertEquals("auth0|12345", result.userId)
        assertEquals("testuser", result.username)
    }

    @Test
    fun `getUserById should throw exception when user not found`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Auth0UserResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        assertThrows(RuntimeException::class.java) {
            auth0ClientService.getUserById("nonexistent-id")
        }
    }

    @Test
    fun `getUsersByEmail should return users with email query`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
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

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = auth0ClientService.getUsersByEmail("test@example.com")

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("test@example.com", result[0].email)
    }

    @Test
    fun `getUsers should handle blank query parameter`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "test-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
            )
        val users = arrayOf<Auth0UserResponseDTO>()

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(Array<Auth0UserResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(users))

        val result = auth0ClientService.getUsers(query = "")

        assertNotNull(result)
        assertEquals(0, result.size)
    }
}
