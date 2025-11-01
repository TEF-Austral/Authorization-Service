package users.dtos

import api.users.dtos.Auth0UserResponseDTO
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
                emailVerified = true,
                username = "testuser",
                phoneNumber = "+1234567890",
                phoneVerified = true,
                createdAt = "2024-01-01T00:00:00.000Z",
                updatedAt = "2024-01-02T00:00:00.000Z",
                identities =
                    listOf(
                        Auth0UserResponseDTO.Identity(
                            connection = "Username-Password-Authentication",
                            userId = "12345",
                            provider = "auth0",
                            isSocial = false,
                        ),
                    ),
                appMetadata = mapOf("role" to "admin"),
                userMetadata = mapOf("preference" to "dark"),
                picture = "https://example.com/pic.jpg",
                name = "Test User",
                nickname = "tester",
                multifactor = listOf("guardian"),
                lastIp = "192.168.1.1",
                lastLogin = "2024-01-02T00:00:00.000Z",
                loginsCount = 5,
                blocked = false,
                givenName = "Test",
                familyName = "User",
            )

        assertNotNull(dto)
        assertEquals("auth0|12345", dto.userId)
        assertEquals("test@example.com", dto.email)
        assertEquals(true, dto.emailVerified)
        assertEquals("testuser", dto.username)
        assertEquals("+1234567890", dto.phoneNumber)
        assertEquals(true, dto.phoneVerified)
        assertEquals("2024-01-01T00:00:00.000Z", dto.createdAt)
        assertEquals("2024-01-02T00:00:00.000Z", dto.updatedAt)
        assertEquals(1, dto.identities?.size)
        assertEquals("Username-Password-Authentication", dto.identities?.get(0)?.connection)
        assertEquals(mapOf("role" to "admin"), dto.appMetadata)
        assertEquals(mapOf("preference" to "dark"), dto.userMetadata)
        assertEquals("https://example.com/pic.jpg", dto.picture)
        assertEquals("Test User", dto.name)
        assertEquals("tester", dto.nickname)
        assertEquals(listOf("guardian"), dto.multifactor)
        assertEquals("192.168.1.1", dto.lastIp)
        assertEquals("2024-01-02T00:00:00.000Z", dto.lastLogin)
        assertEquals(5, dto.loginsCount)
        assertEquals(false, dto.blocked)
        assertEquals("Test", dto.givenName)
        assertEquals("User", dto.familyName)
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
        assertNull(dto.emailVerified)
        assertNull(dto.username)
        assertNull(dto.phoneNumber)
        assertNull(dto.phoneVerified)
        assertNull(dto.createdAt)
        assertNull(dto.updatedAt)
        assertNull(dto.identities)
        assertNull(dto.appMetadata)
        assertNull(dto.userMetadata)
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
        assertNull(dto.emailVerified)
        assertNull(dto.username)
        assertNull(dto.picture)
        assertNull(dto.name)
        assertNull(dto.nickname)
    }

    @Test
    fun `Identity should be created with all fields`() {
        val identity =
            Auth0UserResponseDTO.Identity(
                connection = "google-oauth2",
                userId = "123456",
                provider = "google-oauth2",
                isSocial = true,
            )

        assertNotNull(identity)
        assertEquals("google-oauth2", identity.connection)
        assertEquals("123456", identity.userId)
        assertEquals("google-oauth2", identity.provider)
        assertEquals(true, identity.isSocial)
    }

    @Test
    fun `Identity should be created with minimal fields`() {
        val identity = Auth0UserResponseDTO.Identity()

        assertNotNull(identity)
        assertNull(identity.connection)
        assertNull(identity.userId)
        assertNull(identity.provider)
        assertNull(identity.isSocial)
    }

    @Test
    fun `should handle empty collections`() {
        val dto =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
                identities = emptyList(),
                appMetadata = emptyMap(),
                userMetadata = emptyMap(),
                multifactor = emptyList(),
            )

        assertNotNull(dto)
        assertEquals(0, dto.identities?.size)
        assertEquals(0, dto.appMetadata?.size)
        assertEquals(0, dto.userMetadata?.size)
        assertEquals(0, dto.multifactor?.size)
    }

    @Test
    fun `should handle multiple identities`() {
        val identities =
            listOf(
                Auth0UserResponseDTO.Identity(
                    connection = "google-oauth2",
                    userId = "google123",
                    provider = "google-oauth2",
                    isSocial = true,
                ),
                Auth0UserResponseDTO.Identity(
                    connection = "facebook",
                    userId = "fb456",
                    provider = "facebook",
                    isSocial = true,
                ),
            )

        val dto =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
                identities = identities,
            )

        assertNotNull(dto)
        assertEquals(2, dto.identities?.size)
        assertEquals("google-oauth2", dto.identities?.get(0)?.connection)
        assertEquals("facebook", dto.identities?.get(1)?.connection)
    }

    @Test
    fun `should handle complex metadata`() {
        val appMetadata =
            mapOf(
                "roles" to listOf("admin", "user"),
                "permissions" to mapOf("read" to true, "write" to false),
            )
        val userMetadata =
            mapOf(
                "theme" to "dark",
                "language" to "en",
                "notifications" to true,
            )

        val dto =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "test@example.com",
                appMetadata = appMetadata,
                userMetadata = userMetadata,
            )

        assertNotNull(dto)
        assertNotNull(dto.appMetadata)
        assertNotNull(dto.userMetadata)
        assertEquals(2, dto.appMetadata?.size)
        assertEquals(3, dto.userMetadata?.size)
    }

    @Test
    fun `should handle blocked user`() {
        val dto =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "blocked@example.com",
                blocked = true,
            )

        assertNotNull(dto)
        assertEquals(true, dto.blocked)
    }

    @Test
    fun `should handle unverified email`() {
        val dto =
            Auth0UserResponseDTO(
                userId = "auth0|12345",
                email = "unverified@example.com",
                emailVerified = false,
            )

        assertNotNull(dto)
        assertEquals(false, dto.emailVerified)
    }

    @Test
    fun `should handle user with social identity`() {
        val identity =
            Auth0UserResponseDTO.Identity(
                connection = "google-oauth2",
                userId = "google123",
                provider = "google-oauth2",
                isSocial = true,
            )

        val dto =
            Auth0UserResponseDTO(
                userId = "google-oauth2|google123",
                email = "google@example.com",
                identities = listOf(identity),
            )

        assertNotNull(dto)
        assertEquals("google-oauth2|google123", dto.userId)
        assertEquals(true, dto.identities?.get(0)?.isSocial)
    }
}
