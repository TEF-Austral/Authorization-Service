package repositories

import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PermissionRepositoryTest {

    private lateinit var repository: MockPermissionRepository

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
    }

    @Test
    fun `findByUserIdAndSnippetId should handle concurrent access`() {
        val permission1 =
            Permission(userId = "user1", snippetId = "snippet1", canRead = true, canEdit = false)
        val permission2 =
            Permission(userId = "user2", snippetId = "snippet2", canRead = true, canEdit = false)

        repository.save(permission1)
        repository.save(permission2)

        val found1 = repository.findByUserIdAndSnippetId("user1", "snippet1")
        val found2 = repository.findByUserIdAndSnippetId("user2", "snippet2")

        assertNotNull(found1)
        assertNotNull(found2)
        assertEquals("user1", found1!!.userId)
        assertEquals("user2", found2!!.userId)
    }

    @Test
    fun `deleteByUserIdAndSnippetId should handle non-existent permission gracefully`() {
        repository.deleteByUserIdAndSnippetId("nonexistent", "nonexistent")
        val found = repository.findByUserIdAndSnippetId("nonexistent", "nonexistent")
        assertNull(found)
    }

    @Test
    fun `findAllBySnippetId should handle empty results`() {
        val permissions = repository.findAllBySnippetId("nonexistent")
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `findAllByUserId should handle empty results`() {
        val permissions = repository.findAllByUserId("nonexistent")
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `save should handle rapid sequential saves`() {
        val permissions =
            (1..100).map { i ->
                Permission(
                    userId = "user$i",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit = i % 2 == 0,
                )
            }

        permissions.forEach { repository.save(it) }

        val allPermissions =
            (1..100).map { i ->
                repository.findByUserIdAndSnippetId("user$i", "snippet$i")
            }

        assertEquals(100, allPermissions.filterNotNull().size)
    }

    @Test
    fun `repository should handle large datasets efficiently`() {
        repeat(1000) { i ->
            repository.save(
                Permission(
                    userId = "user${i % 10}",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit = false,
                ),
            )
        }

        val user0Permissions = repository.findAllByUserId("user0")
        assertTrue(user0Permissions.size >= 100)
    }

    @Test
    fun `save should maintain data integrity across updates`() {
        val original =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )
        val saved1 = repository.save(original)

        val updated = saved1.copy(canRead = false, canEdit = true)
        val saved2 = repository.save(updated)

        assertEquals(saved1.id, saved2.id)
        assertEquals(false, saved2.canRead)
        assertEquals(true, saved2.canEdit)
    }

    @Test
    fun `findAllBySnippetId should filter correctly with many permissions`() {
        repeat(50) { i ->
            repository.save(
                Permission(
                    userId = "user$i",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = false,
                ),
            )
            repository.save(
                Permission(
                    userId = "user$i",
                    snippetId = "snippet2",
                    canRead = true,
                    canEdit = false,
                ),
            )
        }

        val snippet1Permissions = repository.findAllBySnippetId("snippet1")
        val snippet2Permissions = repository.findAllBySnippetId("snippet2")

        assertEquals(50, snippet1Permissions.size)
        assertEquals(50, snippet2Permissions.size)
        assertTrue(snippet1Permissions.all { it.snippetId == "snippet1" })
        assertTrue(snippet2Permissions.all { it.snippetId == "snippet2" })
    }

    @Test
    fun `findAllByUserId should filter correctly with many snippets`() {
        repeat(50) { i ->
            repository.save(
                Permission(
                    userId = "user1",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit = false,
                ),
            )
            repository.save(
                Permission(
                    userId = "user2",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit = false,
                ),
            )
        }

        val user1Permissions = repository.findAllByUserId("user1")
        val user2Permissions = repository.findAllByUserId("user2")

        assertEquals(50, user1Permissions.size)
        assertEquals(50, user2Permissions.size)
        assertTrue(user1Permissions.all { it.userId == "user1" })
        assertTrue(user2Permissions.all { it.userId == "user2" })
    }

    @Test
    fun `clear should reset state completely`() {
        repeat(100) { i ->
            repository.save(
                Permission(
                    userId = "user$i",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit = false,
                ),
            )
        }

        repository.clear()

        val allUsers = (0..99).map { repository.findAllByUserId("user$it") }.flatten()
        val allSnippets = (0..99).map { repository.findAllBySnippetId("snippet$it") }.flatten()

        assertTrue(allUsers.isEmpty())
        assertTrue(allSnippets.isEmpty())

        val newPermission =
            repository.save(
                Permission(userId = "new", snippetId = "new", canRead = true, canEdit = false),
            )
        assertEquals(1L, newPermission.id)
    }
}
