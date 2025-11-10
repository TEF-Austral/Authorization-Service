package users.config

import api.users.config.Auth0Config
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class Auth0ConfigTest {

    @Test
    fun `Auth0Config should be created with default values`() {
        val config = Auth0Config()

        assertNotNull(config)
        assertEquals("", config.audience)
        assertEquals("", config.domain)
        assertNotNull(config.management)
    }

    @Test
    fun `Auth0Config should allow setting audience`() {
        val config = Auth0Config()
        config.audience = "https://test-audience.com"

        assertEquals("https://test-audience.com", config.audience)
    }

    @Test
    fun `Auth0Config should allow setting domain`() {
        val config = Auth0Config()
        config.domain = "test-domain.auth0.com"

        assertEquals("test-domain.auth0.com", config.domain)
    }

    @Test
    fun `Auth0Config Management should be created with default values`() {
        val management = Auth0Config.Management()

        assertNotNull(management)
        assertEquals("", management.clientId)
        assertEquals("", management.clientSecret)
    }

    @Test
    fun `Auth0Config Management should allow setting clientId`() {
        val management = Auth0Config.Management()
        management.clientId = "test-client-id"

        assertEquals("test-client-id", management.clientId)
    }

    @Test
    fun `Auth0Config Management should allow setting clientSecret`() {
        val management = Auth0Config.Management()
        management.clientSecret = "test-client-secret"

        assertEquals("test-client-secret", management.clientSecret)
    }

    @Test
    fun `Auth0Config should be created with all properties set`() {
        val config =
            Auth0Config(
                audience = "https://test-audience.com",
                domain = "test-domain.auth0.com",
                management =
                    Auth0Config.Management(
                        clientId = "test-client-id",
                        clientSecret = "test-client-secret",
                    ),
            )

        assertEquals("https://test-audience.com", config.audience)
        assertEquals("test-domain.auth0.com", config.domain)
        assertEquals("test-client-id", config.management.clientId)
        assertEquals("test-client-secret", config.management.clientSecret)
    }

    @Test
    fun `Auth0Config should support data class copy`() {
        val config1 =
            Auth0Config(
                audience = "aud1",
                domain = "domain1",
            )

        val config2 = config1.copy(audience = "aud2")

        assertEquals("aud2", config2.audience)
        assertEquals("domain1", config2.domain)
        assertEquals("aud1", config1.audience)
    }

    @Test
    fun `Auth0Config Management should support data class copy`() {
        val management1 =
            Auth0Config.Management(
                clientId = "id1",
                clientSecret = "secret1",
            )

        val management2 = management1.copy(clientId = "id2")

        assertEquals("id2", management2.clientId)
        assertEquals("secret1", management2.clientSecret)
        assertEquals("id1", management1.clientId)
    }

    @Test
    fun `Auth0Config should support equality comparison`() {
        val config1 =
            Auth0Config(
                audience = "aud",
                domain = "domain",
                management = Auth0Config.Management("id", "secret"),
            )
        val config2 =
            Auth0Config(
                audience = "aud",
                domain = "domain",
                management = Auth0Config.Management("id", "secret"),
            )

        assertEquals(config1, config2)
    }

    @Test
    fun `Auth0Config Management should support equality comparison`() {
        val management1 = Auth0Config.Management("id", "secret")
        val management2 = Auth0Config.Management("id", "secret")

        assertEquals(management1, management2)
    }
}
