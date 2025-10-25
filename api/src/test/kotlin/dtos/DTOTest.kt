package dtos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DTOTest {

    @Test
    fun `CheckPermissionRequestDTO should create with all fields`() {
        val dto =
            CheckPermissionRequestDTO(
                userId = "user1",
                action = "read",
                snippetId = "snippet1",
                ownerId = "owner1",
            )

        assertEquals("user1", dto.userId)
        assertEquals("read", dto.action)
        assertEquals("snippet1", dto.snippetId)
        assertEquals("owner1", dto.ownerId)
    }

    @Test
    fun `CheckPermissionResponseDTO should create with allowed flag`() {
        val dto = CheckPermissionResponseDTO(allowed = true)
        assertTrue(dto.allowed)

        val dto2 = CheckPermissionResponseDTO(allowed = false)
        assertFalse(dto2.allowed)
    }

    @Test
    fun `GrantPermissionRequestDTO should create with all fields`() {
        val dto =
            GrantPermissionRequestDTO(
                requesterId = "requester1",
                ownerId = "owner1",
                granteeId = "grantee1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals("requester1", dto.requesterId)
        assertEquals("owner1", dto.ownerId)
        assertEquals("grantee1", dto.granteeId)
        assertEquals("snippet1", dto.snippetId)
        assertTrue(dto.canRead)
        assertFalse(dto.canEdit)
    }

    @Test
    fun `RevokePermissionRequestDTO should create with all fields`() {
        val dto =
            RevokePermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                requesterId = "requester1",
            )

        assertEquals("user1", dto.userId)
        assertEquals("snippet1", dto.snippetId)
        assertEquals("requester1", dto.requesterId)
    }

    @Test
    fun `PermissionResponseDTO should create with all fields`() {
        val dto =
            PermissionResponseDTO(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals(1L, dto.id)
        assertEquals("user1", dto.userId)
        assertEquals("snippet1", dto.snippetId)
        assertTrue(dto.canRead)
        assertFalse(dto.canEdit)
    }

    @Test
    fun `CreateUserRequestDTO should create with all fields`() {
        val dto =
            CreateUserRequestDTO(
                email = "test@example.com",
                password = "SecurePass123!",
                name = "Test User",
            )

        assertEquals("test@example.com", dto.email)
        assertEquals("SecurePass123!", dto.password)
        assertEquals("Test User", dto.name)
    }

    @Test
    fun `UserResponseDTO should create with all fields`() {
        val dto =
            UserResponseDTO(
                email = "test@example.com",
                name = "Test User",
                userId = "auth0|123456",
            )

        assertEquals("test@example.com", dto.email)
        assertEquals("Test User", dto.name)
        assertEquals("auth0|123456", dto.userId)
    }

    @Test
    fun `GetSnippetPermissionsRequestDTO should create with all fields`() {
        val dto =
            api.dtos.GetSnippetPermissionsRequestDTO(
                snippetId = "snippet1",
                requesterId = "requester1",
            )

        assertEquals("snippet1", dto.snippetId)
        assertEquals("requester1", dto.requesterId)
    }

    @Test
    fun `PermissionResponseDTO should support null id`() {
        val dto =
            PermissionResponseDTO(
                id = null,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals(null, dto.id)
    }

    @Test
    fun `DTOs should support data class equality`() {
        val dto1 =
            CheckPermissionRequestDTO(
                userId = "user1",
                action = "read",
                snippetId = "snippet1",
                ownerId = "owner1",
            )

        val dto2 =
            CheckPermissionRequestDTO(
                userId = "user1",
                action = "read",
                snippetId = "snippet1",
                ownerId = "owner1",
            )

        assertEquals(dto1, dto2)
    }

    @Test
    fun `DTOs should detect inequality`() {
        val dto1 =
            CheckPermissionRequestDTO(
                userId = "user1",
                action = "read",
                snippetId = "snippet1",
                ownerId = "owner1",
            )

        val dto2 =
            CheckPermissionRequestDTO(
                userId = "user2",
                action = "read",
                snippetId = "snippet1",
                ownerId = "owner1",
            )

        assertNotEquals(dto1, dto2)
    }

    @Test
    fun `GrantPermissionRequestDTO should handle both permissions false`() {
        val dto =
            GrantPermissionRequestDTO(
                requesterId = "requester1",
                ownerId = "owner1",
                granteeId = "grantee1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = false,
            )

        assertFalse(dto.canRead)
        assertFalse(dto.canEdit)
    }

    @Test
    fun `GrantPermissionRequestDTO should handle both permissions true`() {
        val dto =
            GrantPermissionRequestDTO(
                requesterId = "requester1",
                ownerId = "owner1",
                granteeId = "grantee1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )

        assertTrue(dto.canRead)
        assertTrue(dto.canEdit)
    }

    @Test
    fun `CheckPermissionRequestDTO should handle different action types`() {
        val actions = listOf("read", "write", "edit", "delete", "share", "execute")

        actions.forEach { action ->
            val dto =
                CheckPermissionRequestDTO(
                    userId = "user1",
                    action = action,
                    snippetId = "snippet1",
                    ownerId = "owner1",
                )

            assertEquals(action, dto.action)
        }
    }

    @Test
    fun `CreateUserRequestDTO should handle special characters in fields`() {
        val dto =
            CreateUserRequestDTO(
                email = "user+tag@example.com",
                password = "P@ssw0rd!#$%",
                name = "Tést Üser-Ñame",
            )

        assertEquals("user+tag@example.com", dto.email)
        assertEquals("P@ssw0rd!#$%", dto.password)
        assertEquals("Tést Üser-Ñame", dto.name)
    }

    @Test
    fun `UserResponseDTO should handle different userId formats`() {
        val userIds =
            listOf(
                "auth0|123456",
                "google-oauth2|987654",
                "github|username",
            )

        userIds.forEach { userId ->
            val dto =
                UserResponseDTO(
                    email = "test@example.com",
                    name = "Test User",
                    userId = userId,
                )

            assertEquals(userId, dto.userId)
        }
    }

    @Test
    fun `PermissionResponseDTO should handle large id values`() {
        val dto =
            PermissionResponseDTO(
                id = Long.MAX_VALUE,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals(Long.MAX_VALUE, dto.id)
    }

    @Test
    fun `DTOs should support copy functionality`() {
        val original =
            GrantPermissionRequestDTO(
                requesterId = "requester1",
                ownerId = "owner1",
                granteeId = "grantee1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val copy = original.copy(canEdit = true)

        assertEquals(original.requesterId, copy.requesterId)
        assertEquals(original.ownerId, copy.ownerId)
        assertEquals(original.granteeId, copy.granteeId)
        assertEquals(original.snippetId, copy.snippetId)
        assertEquals(original.canRead, copy.canRead)
        assertTrue(copy.canEdit)
        assertFalse(original.canEdit)
    }
}
