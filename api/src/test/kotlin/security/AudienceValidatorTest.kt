package security

import api.security.AudienceValidator
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidatorTest {

    private lateinit var validator: AudienceValidator

    @BeforeEach
    fun setUp() {
        validator = AudienceValidator("https://test-audience.com")
    }

    @Test
    fun `validate should return success when audience matches`() {
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("https://test-audience.com"))

        val result = validator.validate(jwt)

        assertNotNull(result)
        assertFalse(result.hasErrors())
    }

    @Test
    fun `validate should return failure when audience does not match`() {
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("https://wrong-audience.com"))

        val result = validator.validate(jwt)

        assertNotNull(result)
        assertTrue(result.hasErrors())
    }

    @Test
    fun `validate should return failure when audience is empty`() {
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(emptyList())

        val result = validator.validate(jwt)

        assertNotNull(result)
        assertTrue(result.hasErrors())
    }

    @Test
    fun `validate should return success when audience is in list of multiple audiences`() {
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(
            listOf(
                "https://other-audience.com",
                "https://test-audience.com",
                "https://another-audience.com",
            ),
        )

        val result = validator.validate(jwt)

        assertNotNull(result)
        assertFalse(result.hasErrors())
    }

    @Test
    fun `validate should return failure with correct error code`() {
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("https://wrong-audience.com"))

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
        val errors = result.errors
        assertTrue(errors.any { it.errorCode == "invalid_token" })
    }

    @Test
    fun `validate should return failure with correct error description`() {
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("https://wrong-audience.com"))

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
        val errors = result.errors
        assertTrue(errors.any { it.description == "The required audience is missing" })
    }

    @Test
    fun `validate should be case sensitive for audience`() {
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("https://TEST-AUDIENCE.COM"))

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
    }

    @Test
    fun `validate should work with different audience format`() {
        val customValidator = AudienceValidator("my-api")
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("my-api"))

        val result = customValidator.validate(jwt)

        assertFalse(result.hasErrors())
    }
}
