package services.authorization

import api.services.authorization.DefaultPermissionQueryService
import api.services.authorization.PermissionMapper
import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.MockPermissionRepository

class DefaultPermissionQueryServiceTest {

    private lateinit var repository: MockPermissionRepository
    private lateinit var mapper: PermissionMapper
    private lateinit var queryService: DefaultPermissionQueryService

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
        mapper = PermissionMapper()
        queryService = DefaultPermissionQueryService(repository, mapper)
    }

    @Test
    fun `getSnippetPermissions should return all permissions for snippet`() {
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

        val result = queryService.getSnippetPermissions("snippet1", "owner1")

        assertNotNull(result)
        assertEquals(2, result.size)
    }

    @Test
    fun `getSnippetPermissions should return empty list when no permissions exist`() {
        val result = queryService.getSnippetPermissions("snippet1", "owner1")

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `getSnippetPermissions should not return permissions for other snippets`() {
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
                canEdit = true,
            ),
        )

        val result = queryService.getSnippetPermissions("snippet1", "owner1")

        assertEquals(1, result.size)
        assertEquals("snippet1", result[0].snippetId)
    }

    @Test
    fun `getUserPermissions should return all permissions for user`() {
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
                canRead = false,
                canEdit = true,
            ),
        )

        val result = queryService.getUserPermissions("user1")

        assertNotNull(result)
        assertEquals(2, result.size)
    }

    @Test
    fun `getUserPermissions should return empty list when user has no permissions`() {
        val result = queryService.getUserPermissions("user1")

        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `getUserPermissions should not return permissions for other users`() {
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
                canEdit = true,
            ),
        )

        val result = queryService.getUserPermissions("user1")

        assertEquals(1, result.size)
        assertEquals("user1", result[0].userId)
    }

    @Test
    fun `getSnippetsByPermission should return snippets user can read`() {
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
                userId = "user1",
                snippetId = "snippet3",
                canRead = false,
                canEdit = true,
            ),
        )

        val result = queryService.getSnippetsByPermission("user1", "read")

        assertEquals(2, result.size)
        assertTrue(result.contains("snippet1"))
        assertTrue(result.contains("snippet2"))
    }

    @Test
    fun `getSnippetsByPermission should return snippets user can edit`() {
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
                userId = "user1",
                snippetId = "snippet3",
                canRead = false,
                canEdit = true,
            ),
        )

        val result = queryService.getSnippetsByPermission("user1", "edit")

        assertEquals(2, result.size)
        assertTrue(result.contains("snippet2"))
        assertTrue(result.contains("snippet3"))
    }

    @Test
    fun `getSnippetsByPermission should handle case insensitive permission type`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val result = queryService.getSnippetsByPermission("user1", "READ")

        assertEquals(1, result.size)
        assertEquals("snippet1", result[0])
    }

    @Test
    fun `getSnippetsByPermission should throw exception for invalid permission type`() {
        assertThrows(IllegalArgumentException::class.java) {
            queryService.getSnippetsByPermission("user1", "invalid")
        }
    }

    @Test
    fun `getSnippetsByPermission should return empty list when user has no matching permissions`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = false,
            ),
        )

        val result = queryService.getSnippetsByPermission("user1", "read")

        assertEquals(0, result.size)
    }

    @Test
    fun `getSnippetsByPermission should return empty list when user has no permissions at all`() {
        val result = queryService.getSnippetsByPermission("user1", "read")

        assertEquals(0, result.size)
    }

    @Test
    fun `getSnippetPermissions should map permissions correctly`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val result = queryService.getSnippetPermissions("snippet1", "owner1")

        assertEquals(1, result.size)
        assertEquals("user1", result[0].userId)
        assertEquals("snippet1", result[0].snippetId)
        assertTrue(result[0].canRead)
        assertEquals(false, result[0].canEdit)
    }

    @Test
    fun `getUserPermissions should map permissions correctly`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = true,
            ),
        )

        val result = queryService.getUserPermissions("user1")

        assertEquals(1, result.size)
        assertEquals("user1", result[0].userId)
        assertEquals("snippet1", result[0].snippetId)
        assertEquals(false, result[0].canRead)
        assertTrue(result[0].canEdit)
    }

    @Test
    fun `getSnippetsByPermission should handle EDIT in uppercase`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            ),
        )

        val result = queryService.getSnippetsByPermission("user1", "EDIT")

        assertEquals(1, result.size)
    }

    @Test
    fun `getSnippetsByPermission should handle mixed case permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val result = queryService.getSnippetsByPermission("user1", "ReAd")

        assertEquals(1, result.size)
    }
}
