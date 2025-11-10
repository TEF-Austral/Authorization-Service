package dtos

import api.dtos.responses.Auth0UserResponseDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class Auth0UserResponseDTOTest {

    @Test
    fun `should create Auth0UserResponseDTO with all fields`() {
        val dto =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
                username = "testuser",
                picture = "https://example.com/pic.jpg",
                name = "Test User",
                nickname = "tester",
            )

        assertNotNull(dto)
        assertEquals("auth0|12345", dto.userId)
        assertEquals("test@example.com", dto.email)
        assertEquals("testuser", dto.username)
        assertEquals("https://example.com/pic.jpg", dto.picture)
        assertEquals("Test User", dto.name)
        assertEquals("tester", dto.nickname)
    }

    @Test
    fun `should create Auth0UserResponseDTO with minimal fields`() {
        val dto =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
            )

        assertNotNull(dto)
        assertEquals("auth0|12345", dto.userId)
        assertEquals("test@example.com", dto.email)
        assertNull(dto.username)
        assertNull(dto.picture)
        assertNull(dto.name)
        assertNull(dto.nickname)
    }

    @Test
    fun `should create Auth0UserResponseDTO with null values`() {
        val dto = Auth0UserResponseDTO()

        assertNotNull(dto)
        assertNull(dto.userId)
        assertNull(dto.email)
        assertNull(dto.username)
        assertNull(dto.picture)
        assertNull(dto.name)
        assertNull(dto.nickname)
    }
}
