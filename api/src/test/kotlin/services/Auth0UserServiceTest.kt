package services

import dtos.CreateUserRequestDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class Auth0UserServiceTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var service: Auth0UserService
    private val domain = "test-domain.auth0.com"
    private val clientId = "test-client-id"
    private val clientSecret = "test-client-secret"

    @BeforeEach
    fun setUp() {
        restTemplate = mock()
        service = Auth0UserService(restTemplate, domain, clientId, clientSecret)
    }

    @Test
    fun `createUser should successfully create user in Auth0`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                eq("https://$domain/oauth/token"),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        val userResponse =
            mapOf(
                "email" to "test@example.com",
                "name" to "Test User",
                "user_id" to "auth0|123456",
            )
        val userEntity = ResponseEntity.ok(userResponse as Map<*, *>)

        whenever(
            restTemplate.exchange(
                eq("https://$domain/api/v2/users"),
                eq(HttpMethod.POST),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(userEntity)

        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "SecurePassword123!",
                name = "Test User",
            )
        val result = service.createUser(request)

        assertEquals("test@example.com", result.email)
        assertEquals("Test User", result.name)
        assertEquals("auth0|123456", result.userId)

        verify(restTemplate).postForEntity(
            eq("https://$domain/oauth/token"),
            any(),
            eq(Map::class.java),
        )
        verify(restTemplate).exchange(
            eq("https://$domain/api/v2/users"),
            eq(HttpMethod.POST),
            any(),
            eq(Map::class.java),
        )
    }

    @Test
    fun `createUser should throw exception when token retrieval fails`() {
        whenever(
            restTemplate.postForEntity(
                any<String>(),
                any(),
                eq(Map::class.java),
            ),
        ).thenThrow(RuntimeException("Token retrieval failed"))

        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "password",
                name = "Test User",
            )

        assertThrows<RuntimeException> {
            service.createUser(request)
        }
    }

    @Test
    fun `createUser should throw exception when user creation fails`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                eq("https://$domain/oauth/token"),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        whenever(
            restTemplate.exchange(
                eq("https://$domain/api/v2/users"),
                eq(HttpMethod.POST),
                any(),
                eq(Map::class.java),
            ),
        ).thenThrow(RuntimeException("User creation failed"))

        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "password",
                name = "Test User",
            )

        assertThrows<RuntimeException> {
            service.createUser(request)
        }
    }

    @Test
    fun `deleteUser should successfully delete user from Auth0`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                eq("https://$domain/oauth/token"),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        val deleteEntity = ResponseEntity.ok().build<Void>()

        whenever(
            restTemplate.exchange(
                eq("https://$domain/api/v2/users/auth0|123456"),
                eq(HttpMethod.DELETE),
                any(),
                eq(Void::class.java),
            ),
        ).thenReturn(deleteEntity)

        service.deleteUser("auth0|123456")

        verify(restTemplate).exchange(
            eq("https://$domain/api/v2/users/auth0|123456"),
            eq(HttpMethod.DELETE),
            any(),
            eq(Void::class.java),
        )
    }

    @Test
    fun `deleteUser should throw exception when token retrieval fails`() {
        whenever(
            restTemplate.postForEntity(
                any<String>(),
                any(),
                eq(Map::class.java),
            ),
        ).thenThrow(RuntimeException("Token retrieval failed"))

        assertThrows<RuntimeException> {
            service.deleteUser("auth0|123456")
        }
    }

    @Test
    fun `deleteUser should throw exception when delete fails`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                eq("https://$domain/oauth/token"),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.DELETE),
                any(),
                eq(Void::class.java),
            ),
        ).thenThrow(RuntimeException("Delete failed"))

        assertThrows<RuntimeException> {
            service.deleteUser("auth0|123456")
        }
    }

    @Test
    fun `createUser should send correct request body`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                eq("https://$domain/oauth/token"),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        val userResponse =
            mapOf(
                "email" to "test@example.com",
                "name" to "Test User",
                "user_id" to "auth0|123",
            )
        val userEntity = ResponseEntity.ok(userResponse as Map<*, *>)

        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(userEntity)

        val request =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "password",
                name = "Test User",
            )

        service.createUser(request)

        verify(restTemplate).exchange(
            eq("https://$domain/api/v2/users"),
            eq(HttpMethod.POST),
            argThat<HttpEntity<Map<String, String>>> { entity ->
                val body = entity.body
                body != null &&
                    body["email"] == "test@example.com" &&
                    body["password"] == "password" &&
                    body["name"] == "Test User" &&
                    body["connection"] == "Username-Password-Authentication"
            },
            eq(Map::class.java),
        )
    }

    @Test
    fun `getManagementToken should request correct token parameters`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                any<String>(),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        val userResponse =
            mapOf(
                "email" to "test@example.com",
                "name" to "Test",
                "user_id" to "auth0|123",
            )
        val userEntity = ResponseEntity.ok(userResponse as Map<*, *>)

        whenever(
            restTemplate.exchange(
                any<String>(),
                any(),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(userEntity)

        service.createUser(CreateUserRequestDTO("test@example.com", "password", "Test"))

        verify(restTemplate).postForEntity(
            eq("https://$domain/oauth/token"),
            argThat<HttpEntity<Map<String, String>>> { entity ->
                val body = entity.body
                body != null &&
                    body["client_id"] == clientId &&
                    body["client_secret"] == clientSecret &&
                    body["audience"] == "https://$domain/api/v2/" &&
                    body["grant_type"] == "client_credentials"
            },
            eq(Map::class.java),
        )
    }

    @Test
    fun `deleteUser should handle special characters in userId`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                any<String>(),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        val deleteEntity = ResponseEntity.ok().build<Void>()

        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.DELETE),
                any(),
                eq(Void::class.java),
            ),
        ).thenReturn(deleteEntity)

        val userIdWithSpecialChars = "auth0|user%20with%20spaces"

        service.deleteUser(userIdWithSpecialChars)

        verify(restTemplate).exchange(
            eq("https://$domain/api/v2/users/$userIdWithSpecialChars"),
            eq(HttpMethod.DELETE),
            any(),
            eq(Void::class.java),
        )
    }

    @Test
    fun `createUser should set correct headers for token request`() {
        val tokenResponse = mapOf("access_token" to "mock-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                any<String>(),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        val userResponse =
            mapOf(
                "email" to "test@example.com",
                "name" to "Test",
                "user_id" to "auth0|123",
            )
        val userEntity = ResponseEntity.ok(userResponse as Map<*, *>)

        whenever(
            restTemplate.exchange(
                any<String>(),
                any(),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(userEntity)

        service.createUser(CreateUserRequestDTO("test@example.com", "password", "Test"))

        verify(restTemplate).postForEntity(
            any<String>(),
            argThat<HttpEntity<Map<String, String>>> { entity ->
                entity.headers.contentType == MediaType.APPLICATION_JSON
            },
            eq(Map::class.java),
        )
    }

    @Test
    fun `createUser should set bearer token in user creation request`() {
        val tokenResponse = mapOf("access_token" to "test-bearer-token")
        val tokenEntity = ResponseEntity.ok(tokenResponse as Map<*, *>)

        whenever(
            restTemplate.postForEntity(
                any<String>(),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(tokenEntity)

        val userResponse =
            mapOf(
                "email" to "test@example.com",
                "name" to "Test",
                "user_id" to "auth0|123",
            )
        val userEntity = ResponseEntity.ok(userResponse as Map<*, *>)

        whenever(
            restTemplate.exchange(
                any<String>(),
                any(),
                any(),
                eq(Map::class.java),
            ),
        ).thenReturn(userEntity)

        service.createUser(CreateUserRequestDTO("test@example.com", "password", "Test"))

        verify(restTemplate).exchange(
            any<String>(),
            any(),
            argThat<HttpEntity<Map<String, String>>> { entity ->
                entity.headers.getFirst("Authorization") == "Bearer test-bearer-token"
            },
            eq(Map::class.java),
        )
    }
}
