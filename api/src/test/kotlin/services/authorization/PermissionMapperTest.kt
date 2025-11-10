package services.authorization

import api.services.authorization.PermissionMapper
import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class PermissionMapperTest {

    private val mapper = PermissionMapper()

    @Test
    fun `toDTO should map all fields correctly`() {
        val permission =
            Permission(
                id = 1L,
                userId = "user123",
                snippetId = "snippet456",
                canRead = true,
                canEdit = false,
            )

        val dto = mapper.toDTO(permission)

        assertNotNull(dto)
        assertEquals(1L, dto.id)
        assertEquals("user123", dto.userId)
        assertEquals("snippet456", dto.snippetId)
        assertEquals(true, dto.canRead)
        assertEquals(false, dto.canEdit)
    }

    @Test
    fun `toDTO should handle permission with both read and edit true`() {
        val permission =
            Permission(
                id = 2L,
                userId = "user456",
                snippetId = "snippet789",
                canRead = true,
                canEdit = true,
            )

        val dto = mapper.toDTO(permission)

        assertEquals(2L, dto.id)
        assertEquals("user456", dto.userId)
        assertEquals("snippet789", dto.snippetId)
        assertEquals(true, dto.canRead)
        assertEquals(true, dto.canEdit)
    }

    @Test
    fun `toDTO should handle permission with both read and edit false`() {
        val permission =
            Permission(
                id = 3L,
                userId = "user789",
                snippetId = "snippet012",
                canRead = false,
                canEdit = false,
            )

        val dto = mapper.toDTO(permission)

        assertEquals(3L, dto.id)
        assertEquals("user789", dto.userId)
        assertEquals("snippet012", dto.snippetId)
        assertEquals(false, dto.canRead)
        assertEquals(false, dto.canEdit)
    }

    @Test
    fun `toDTO should handle permission with null id`() {
        val permission =
            Permission(
                id = null,
                userId = "user000",
                snippetId = "snippet000",
                canRead = true,
                canEdit = false,
            )

        val dto = mapper.toDTO(permission)

        assertEquals(null, dto.id)
        assertEquals("user000", dto.userId)
        assertEquals("snippet000", dto.snippetId)
    }

    @Test
    fun `toDTO should handle long user and snippet IDs`() {
        val longUserId = "user" + "a".repeat(100)
        val longSnippetId = "snippet" + "b".repeat(100)

        val permission =
            Permission(
                id = 5L,
                userId = longUserId,
                snippetId = longSnippetId,
                canRead = true,
                canEdit = true,
            )

        val dto = mapper.toDTO(permission)

        assertEquals(longUserId, dto.userId)
        assertEquals(longSnippetId, dto.snippetId)
    }

    @Test
    fun `toDTO should handle special characters in IDs`() {
        val permission =
            Permission(
                id = 6L,
                userId = "user-123_test@domain",
                snippetId = "snippet-456_test#special",
                canRead = false,
                canEdit = true,
            )

        val dto = mapper.toDTO(permission)

        assertEquals("user-123_test@domain", dto.userId)
        assertEquals("snippet-456_test#special", dto.snippetId)
    }

    @Test
    fun `toDTO should create independent DTO from permission`() {
        val permission =
            Permission(
                id = 7L,
                userId = "user777",
                snippetId = "snippet888",
                canRead = true,
                canEdit = false,
            )

        val dto = mapper.toDTO(permission)

        assertEquals(permission.id, dto.id)
        assertEquals(permission.userId, dto.userId)
        assertEquals(permission.snippetId, dto.snippetId)
        assertEquals(permission.canRead, dto.canRead)
        assertEquals(permission.canEdit, dto.canEdit)
    }
}
