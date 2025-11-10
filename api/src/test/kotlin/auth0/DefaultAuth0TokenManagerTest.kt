package auth0

import api.auth0.DefaultAuth0TokenManager
import api.dtos.responses.Auth0TokenResponseDTO
import api.users.config.Auth0Config
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
import org.springframework.web.client.RestTemplate

class DefaultAuth0TokenManagerTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var auth0Config: Auth0Config
    private lateinit var tokenManager: DefaultAuth0TokenManager

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
        tokenManager = DefaultAuth0TokenManager(restTemplate, auth0Config)
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

        val token = tokenManager.getManagementApiToken()

        assertNotNull(token)
        assertEquals("test-access-token", token)
        verify(restTemplate).postForObject(
            any<String>(),
            any<HttpEntity<*>>(),
            eq(Auth0TokenResponseDTO::class.java),
        )
    }

    @Test
    fun `getManagementApiToken should cache token when not expired`() {
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

        val token1 = tokenManager.getManagementApiToken()
        val token2 = tokenManager.getManagementApiToken()
        val token3 = tokenManager.getManagementApiToken()

        assertEquals(token1, token2)
        assertEquals(token1, token3)
        verify(restTemplate, times(1)).postForObject(
            any<String>(),
            any<HttpEntity<*>>(),
            eq(Auth0TokenResponseDTO::class.java),
        )
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

        val exception =
            assertThrows(RuntimeException::class.java) {
                tokenManager.getManagementApiToken()
            }

        assertEquals("Failed to obtain Auth0 Management API token", exception.message)
    }

    @Test
    fun `getManagementApiToken should use correct URL with domain`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "token123",
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

        tokenManager.getManagementApiToken()

        verify(restTemplate).postForObject(
            eq("https://test-domain.auth0.com/oauth/token"),
            any<HttpEntity<*>>(),
            eq(Auth0TokenResponseDTO::class.java),
        )
    }

    @Test
    fun `getManagementApiToken should refresh token after expiration`() {
        val tokenResponse1 =
            Auth0TokenResponseDTO(
                accessToken = "token1",
                expiresIn = 0,
                tokenType = "Bearer",
            )
        val tokenResponse2 =
            Auth0TokenResponseDTO(
                accessToken = "token2",
                expiresIn = 3600,
                tokenType = "Bearer",
            )

        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse1, tokenResponse2)

        val token1 = tokenManager.getManagementApiToken()
        Thread.sleep(100)
        val token2 = tokenManager.getManagementApiToken()

        assertEquals("token1", token1)
        assertEquals("token2", token2)
        verify(restTemplate, times(2)).postForObject(
            any<String>(),
            any<HttpEntity<*>>(),
            eq(Auth0TokenResponseDTO::class.java),
        )
    }

    @Test
    fun `getManagementApiToken should handle long expiration times`() {
        val tokenResponse =
            Auth0TokenResponseDTO(
                accessToken = "long-lived-token",
                expiresIn = 86400,
                tokenType = "Bearer",
            )
        whenever(
            restTemplate.postForObject(
                any<String>(),
                any<HttpEntity<*>>(),
                eq(Auth0TokenResponseDTO::class.java),
            ),
        ).thenReturn(tokenResponse)

        val token1 = tokenManager.getManagementApiToken()
        val token2 = tokenManager.getManagementApiToken()

        assertEquals("long-lived-token", token1)
        assertEquals("long-lived-token", token2)
        verify(restTemplate, times(1)).postForObject(
            any<String>(),
            any<HttpEntity<*>>(),
            eq(Auth0TokenResponseDTO::class.java),
        )
    }
}
