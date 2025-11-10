package dtos

import api.dtos.responses.Auth0TokenResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class Auth0TokenResponseDTOTest {

    @Test
    fun `should create Auth0TokenResponseDTO with all fields`() {
        val dto =
            Auth0TokenResponseDTO(
                accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
                expiresIn = 86400L,
                tokenType = "Bearer",
            )

        assertNotNull(dto)
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", dto.accessToken)
        assertEquals(86400L, dto.expiresIn)
        assertEquals("Bearer", dto.tokenType)
    }

    @Test
    fun `should create Auth0TokenResponseDTO with short expiration`() {
        val dto =
            Auth0TokenResponseDTO(
                accessToken = "short_token",
                expiresIn = 3600L,
                tokenType = "Bearer",
            )

        assertNotNull(dto)
        assertEquals("short_token", dto.accessToken)
        assertEquals(3600L, dto.expiresIn)
    }

    @Test
    fun `should create Auth0TokenResponseDTO with long expiration`() {
        val dto =
            Auth0TokenResponseDTO(
                accessToken = "long_token",
                expiresIn = 2592000L,
                tokenType = "Bearer",
            )

        assertNotNull(dto)
        assertEquals(2592000L, dto.expiresIn)
    }

    @Test
    fun `should create Auth0TokenResponseDTO with different token type`() {
        val dto =
            Auth0TokenResponseDTO(
                accessToken = "access_token",
                expiresIn = 86400L,
                tokenType = "JWT",
            )

        assertNotNull(dto)
        assertEquals("JWT", dto.tokenType)
    }

    @Test
    fun `should handle long access token`() {
        val longToken = "a".repeat(500)

        val dto =
            Auth0TokenResponseDTO(
                accessToken = longToken,
                expiresIn = 86400L,
                tokenType = "Bearer",
            )

        assertNotNull(dto)
        assertEquals(500, dto.accessToken.length)
    }

    @Test
    fun `Auth0TokenResponseDTO data class should support equality`() {
        val dto1 =
            Auth0TokenResponseDTO(
                accessToken = "token123",
                expiresIn = 86400L,
                tokenType = "Bearer",
            )
        val dto2 =
            Auth0TokenResponseDTO(
                accessToken = "token123",
                expiresIn = 86400L,
                tokenType = "Bearer",
            )

        assertEquals(dto1, dto2)
    }

    @Test
    fun `Auth0TokenResponseDTO data class should support copy`() {
        val original =
            Auth0TokenResponseDTO(
                accessToken = "original_token",
                expiresIn = 86400L,
                tokenType = "Bearer",
            )

        val copied = original.copy(accessToken = "new_token")

        assertEquals("new_token", copied.accessToken)
        assertEquals(86400L, copied.expiresIn)
        assertEquals("Bearer", copied.tokenType)
    }
}
