package entities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PermissionEntityTest {

    @Test
    fun `Permission should create with all fields`() {
        val permission =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals(1L, permission.id)
        assertEquals("user1", permission.userId)
        assertEquals("snippet1", permission.snippetId)
        assertTrue(permission.canRead)
        assertFalse(permission.canEdit)
    }

    @Test
    fun `Permission should create with null id`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertNull(permission.id)
        assertEquals("user1", permission.userId)
        assertEquals("snippet1", permission.snippetId)
    }

    @Test
    fun `Permission should create with default values`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
            )

        assertFalse(permission.canRead)
        assertFalse(permission.canEdit)
    }

    @Test
    fun `Permission should support copy with changes`() {
        val original =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val copy = original.copy(canEdit = true)

        assertEquals(original.id, copy.id)
        assertEquals(original.userId, copy.userId)
        assertEquals(original.snippetId, copy.snippetId)
        assertEquals(original.canRead, copy.canRead)
        assertTrue(copy.canEdit)
        assertFalse(original.canEdit)
    }

    @Test
    fun `Permission should support equality`() {
        val permission1 =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val permission2 =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals(permission1, permission2)
    }

    @Test
    fun `Permission should detect inequality when fields differ`() {
        val permission1 =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val permission2 =
            Permission(
                id = 2L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertNotEquals(permission1, permission2)
    }

    @Test
    fun `Permission should handle both permissions true`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )

        assertTrue(permission.canRead)
        assertTrue(permission.canEdit)
    }

    @Test
    fun `Permission should handle both permissions false`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = false,
            )

        assertFalse(permission.canRead)
        assertFalse(permission.canEdit)
    }

    @Test
    fun `Permission should handle special characters in userId`() {
        val permission =
            Permission(
                userId = "auth0|user-123_test",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals("auth0|user-123_test", permission.userId)
    }

    @Test
    fun `Permission should handle special characters in snippetId`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet-123_test",
                canRead = true,
                canEdit = false,
            )

        assertEquals("snippet-123_test", permission.snippetId)
    }

    @Test
    fun `Permission should handle long id values`() {
        val permission =
            Permission(
                id = Long.MAX_VALUE,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals(Long.MAX_VALUE, permission.id)
    }

    @Test
    fun `Permission should support copy with id change`() {
        val original =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val copy = original.copy(id = 42L)

        assertEquals(42L, copy.id)
        assertNull(original.id)
    }

    @Test
    fun `Permission should support copy with userId change`() {
        val original =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val copy = original.copy(userId = "user2")

        assertEquals("user2", copy.userId)
        assertEquals("user1", original.userId)
    }

    @Test
    fun `Permission should support copy with snippetId change`() {
        val original =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val copy = original.copy(snippetId = "snippet2")

        assertEquals("snippet2", copy.snippetId)
        assertEquals("snippet1", original.snippetId)
    }

    @Test
    fun `Different permissions same userId and snippetId`() {
        val permission1 =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val permission2 =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )

        assertNotEquals(permission1, permission2)
    }

    @Test
    fun `Permission should have consistent hashCode for equal objects`() {
        val permission1 =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val permission2 =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertEquals(permission1.hashCode(), permission2.hashCode())
    }

    @Test
    fun `Permission should support toString`() {
        val permission =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val string = permission.toString()

        assertTrue(string.contains("user1"))
        assertTrue(string.contains("snippet1"))
    }
}
