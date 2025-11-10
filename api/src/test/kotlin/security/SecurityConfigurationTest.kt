package security

import api.config.RestTemplateConfig
import api.security.AudienceValidator
import api.security.JacksonConfig
import api.security.OAuth2ResourceServerSecurityConfiguration
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SecurityConfigurationTest {

    @Test
    fun `RestTemplateConfig should create RestTemplate bean`() {
        val config = RestTemplateConfig()
        val restTemplate = config.restTemplate()
        assertNotNull(restTemplate)
    }

    @Test
    fun `JacksonConfig should create ObjectMapper bean`() {
        val config = JacksonConfig()
        val objectMapper = config.objectMapper()
        assertNotNull(objectMapper)
    }

    @Test
    fun `AudienceValidator should be instantiable with audience`() {
        assertDoesNotThrow {
            AudienceValidator("test-audience")
        }
    }

    @Test
    fun `AudienceValidator should be instantiable with different audiences`() {
        assertDoesNotThrow {
            AudienceValidator("audience1")
            AudienceValidator("audience2")
            AudienceValidator("https://api.example.com")
        }
    }

    @Test
    fun `OAuth2ResourceServerSecurityConfiguration should be instantiable`() {
        assertDoesNotThrow {
            OAuth2ResourceServerSecurityConfiguration(
                audience = "test-audience",
                issuer = "https://test.auth0.com/",
            )
        }
    }

    @Test
    fun `OAuth2ResourceServerSecurityConfiguration should accept different configurations`() {
        assertDoesNotThrow {
            OAuth2ResourceServerSecurityConfiguration(
                audience = "api1",
                issuer = "https://issuer1.com/",
            )
            OAuth2ResourceServerSecurityConfiguration(
                audience = "api2",
                issuer = "https://issuer2.com/",
            )
        }
    }

    @Test
    fun `RestTemplateConfig creates non-null instance`() {
        val config = RestTemplateConfig()
        assertNotNull(config)
    }

    @Test
    fun `JacksonConfig creates non-null instance`() {
        val config = JacksonConfig()
        assertNotNull(config)
    }
}
