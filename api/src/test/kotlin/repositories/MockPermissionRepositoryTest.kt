package repositories

import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MockPermissionRepositoryTest {

    private lateinit var repository: MockPermissionRepository

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
    }

    @Test
    fun `save should assign id to new permission`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val saved = repository.save(permission)

        assertNotNull(saved.id)
        assertEquals(1L, saved.id)
    }

    @Test
    fun `save should increment id for multiple permissions`() {
        val permission1 =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )
        val permission2 =
            Permission(
                userId = "user2",
                snippetId = "snippet2",
                canRead = true,
                canEdit = true,
            )

        val saved1 = repository.save(permission1)
        val saved2 = repository.save(permission2)

        assertEquals(1L, saved1.id)
        assertEquals(2L, saved2.id)
    }

    @Test
    fun `save should preserve existing id when updating`() {
        val permission =
            Permission(
                id = 99L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val saved = repository.save(permission)

        assertEquals(99L, saved.id)
    }

    @Test
    fun `save should update existing permission`() {
        val permission1 =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )
        repository.save(permission1)

        val permission2 =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )
        val updated = repository.save(permission2)

        assertTrue(updated.canEdit)

        val found = repository.findByUserIdAndSnippetId("user1", "snippet1")
        assertNotNull(found)
        assertTrue(found!!.canEdit)
    }

    @Test
    fun `findByUserIdAndSnippetId should return permission when exists`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )
        repository.save(permission)

        val found = repository.findByUserIdAndSnippetId("user1", "snippet1")

        assertNotNull(found)
        assertEquals("user1", found!!.userId)
        assertEquals("snippet1", found.snippetId)
    }

    @Test
    fun `findByUserIdAndSnippetId should return null when not exists`() {
        val found = repository.findByUserIdAndSnippetId("user1", "snippet1")

        assertNull(found)
    }

    @Test
    fun `deleteByUserIdAndSnippetId should remove permission`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )
        repository.save(permission)

        repository.deleteByUserIdAndSnippetId("user1", "snippet1")

        val found = repository.findByUserIdAndSnippetId("user1", "snippet1")
        assertNull(found)
    }

    @Test
    fun `deleteByUserIdAndSnippetId should not affect other permissions`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )
        repository.save(
            Permission(
                userId = "user2",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        repository.deleteByUserIdAndSnippetId("user1", "snippet1")

        val found1 = repository.findByUserIdAndSnippetId("user1", "snippet1")
        val found2 = repository.findByUserIdAndSnippetId("user2", "snippet1")

        assertNull(found1)
        assertNotNull(found2)
    }

    @Test
    fun `findAllBySnippetId should return all permissions for snippet`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )
        repository.save(
            Permission(
                userId = "user2",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            ),
        )
        repository.save(
            Permission(
                userId = "user3",
                snippetId = "snippet2",
                canRead = true,
                canEdit = false,
            ),
        )

        val permissions = repository.findAllBySnippetId("snippet1")

        assertEquals(2, permissions.size)
        assertTrue(permissions.any { it.userId == "user1" })
        assertTrue(permissions.any { it.userId == "user2" })
    }

    @Test
    fun `findAllBySnippetId should return empty list when no permissions`() {
        val permissions = repository.findAllBySnippetId("snippet1")

        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `findAllByUserId should return all permissions for user`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet2",
                canRead = true,
                canEdit = true,
            ),
        )
        repository.save(
            Permission(
                userId = "user2",
                snippetId = "snippet3",
                canRead = true,
                canEdit = false,
            ),
        )

        val permissions = repository.findAllByUserId("user1")

        assertEquals(2, permissions.size)
        assertTrue(permissions.any { it.snippetId == "snippet1" })
        assertTrue(permissions.any { it.snippetId == "snippet2" })
    }

    @Test
    fun `findAllByUserId should return empty list when no permissions`() {
        val permissions = repository.findAllByUserId("user1")

        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `clear should remove all permissions`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )
        repository.save(
            Permission(
                userId = "user2",
                snippetId = "snippet2",
                canRead = true,
                canEdit = false,
            ),
        )

        repository.clear()

        val permissions1 = repository.findAllByUserId("user1")
        val permissions2 = repository.findAllByUserId("user2")

        assertTrue(permissions1.isEmpty())
        assertTrue(permissions2.isEmpty())
    }

    @Test
    fun `clear should reset id counter`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        repository.clear()

        val newPermission =
            repository.save(
                Permission(
                    userId = "user2",
                    snippetId = "snippet2",
                    canRead = true,
                    canEdit = false,
                ),
            )

        assertEquals(1L, newPermission.id)
    }

    @Test
    fun `repository should handle multiple permissions with same user but different snippets`() {
        for (i in 1..5) {
            repository.save(
                Permission(
                    userId = "user1",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit = i % 2 == 0,
                ),
            )
        }

        val permissions = repository.findAllByUserId("user1")
        assertEquals(5, permissions.size)
    }

    @Test
    fun `repository should handle multiple permissions with same snippet but different users`() {
        for (i in 1..5) {
            repository.save(
                Permission(
                    userId = "user$i",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = i % 2 == 0,
                ),
            )
        }

        val permissions = repository.findAllBySnippetId("snippet1")
        assertEquals(5, permissions.size)
    }

    @Test
    fun `save should handle permission with canRead false and canEdit false`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = false,
            )

        val saved = repository.save(permission)

        assertNotNull(saved.id)
        assertEquals(false, saved.canRead)
        assertEquals(false, saved.canEdit)
    }

    @Test
    fun `save should handle permission with both canRead and canEdit true`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )

        val saved = repository.save(permission)

        assertNotNull(saved.id)
        assertTrue(saved.canRead)
        assertTrue(saved.canEdit)
    }
}
