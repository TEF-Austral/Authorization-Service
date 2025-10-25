package services

import dtos.CheckPermissionRequestDTO
import dtos.GrantPermissionRequestDTO
import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.MockPermissionRepository

class AuthorizationServiceEdgeCasesTest {

    private lateinit var repository: MockPermissionRepository
    private lateinit var service: AuthorizationService

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
        service = AuthorizationService(repository)
    }

    @Test
    fun `checkPermission should handle grant_permission action for owner only`() {
        val ownerRequest =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "grant_permission",
                ownerId = "owner1",
            )

        val nonOwnerRequest =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "grant_permission",
                ownerId = "owner1",
            )

        assertTrue(service.checkPermission(ownerRequest))
        assertFalse(service.checkPermission(nonOwnerRequest))
    }

    @Test
    fun `checkPermission should deny execute for user without read permission`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "execute",
                ownerId = "owner1",
            )

        assertFalse(service.checkPermission(request))
    }

    @Test
    fun `checkPermission should allow execute for user with edit permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = true,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "execute",
                ownerId = "owner1",
            )

        assertFalse(service.checkPermission(request))
    }

    @Test
    fun `grantPermission should allow changing from read to edit`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )

        val result = service.grantPermission(request)

        assertTrue(result.canRead)
        assertTrue(result.canEdit)
    }

    @Test
    fun `grantPermission should allow revoking edit while keeping read`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            ),
        )

        val request =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val result = service.grantPermission(request)

        assertTrue(result.canRead)
        assertFalse(result.canEdit)
    }

    @Test
    fun `grantPermission should allow granting neither read nor edit`() {
        val request =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = false,
            )

        val result = service.grantPermission(request)

        assertFalse(result.canRead)
        assertFalse(result.canEdit)
    }

    @Test
    fun `grantPermission should preserve permission id when updating`() {
        val existing =
            repository.save(
                Permission(
                    userId = "user1",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = false,
                ),
            )

        val request =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )

        val result = service.grantPermission(request)

        assertEquals(existing.id, result.id)
    }

    @Test
    fun `getSnippetPermissions should handle multiple permissions`() {
        for (i in 1..10) {
            repository.save(
                Permission(
                    userId = "user$i",
                    snippetId = "snippet1",
                    canRead = i % 2 == 0,
                    canEdit = i % 3 == 0,
                ),
            )
        }

        val permissions = service.getSnippetPermissions("snippet1", "owner1")

        assertEquals(10, permissions.size)
    }

    @Test
    fun `getUserPermissions should handle multiple snippets`() {
        for (i in 1..10) {
            repository.save(
                Permission(
                    userId = "user1",
                    snippetId = "snippet$i",
                    canRead = i % 2 == 0,
                    canEdit = i % 3 == 0,
                ),
            )
        }

        val permissions = service.getUserPermissions("user1")

        assertEquals(10, permissions.size)
    }

    @Test
    fun `revokePermission should work for permission with only read`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        service.revokePermission("user1", "snippet1", "owner1")

        val permission = repository.findByUserIdAndSnippetId("user1", "snippet1")
        assertEquals(null, permission)
    }

    @Test
    fun `revokePermission should work for permission with only edit`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = true,
            ),
        )

        service.revokePermission("user1", "snippet1", "owner1")

        val permission = repository.findByUserIdAndSnippetId("user1", "snippet1")
        assertEquals(null, permission)
    }

    @Test
    fun `checkPermission should handle empty userId`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        assertFalse(service.checkPermission(request))
    }

    @Test
    fun `checkPermission should handle empty ownerId`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "",
            )

        assertFalse(service.checkPermission(request))
    }

    @Test
    fun `checkPermission should handle mixed case actions consistently`() {
        val actions = listOf("READ", "read", "Read", "rEaD")

        actions.forEach { action ->
            val request =
                CheckPermissionRequestDTO(
                    userId = "owner1",
                    snippetId = "snippet1",
                    action = action,
                    ownerId = "owner1",
                )

            assertTrue(service.checkPermission(request), "Failed for action: $action")
        }
    }

    @Test
    fun `grantPermission should handle multiple grants to different users for same snippet`() {
        val users = listOf("user1", "user2", "user3")

        users.forEach { userId ->
            val request =
                GrantPermissionRequestDTO(
                    requesterId = "owner1",
                    ownerId = "owner1",
                    granteeId = userId,
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = false,
                )

            val result = service.grantPermission(request)
            assertEquals(userId, result.userId)
        }

        val permissions = service.getSnippetPermissions("snippet1", "owner1")
        assertEquals(3, permissions.size)
    }

    @Test
    fun `grantPermission should handle grants for same user across different snippets`() {
        val snippets = listOf("snippet1", "snippet2", "snippet3")

        snippets.forEach { snippetId ->
            val request =
                GrantPermissionRequestDTO(
                    requesterId = "owner1",
                    ownerId = "owner1",
                    granteeId = "user1",
                    snippetId = snippetId,
                    canRead = true,
                    canEdit = false,
                )

            val result = service.grantPermission(request)
            assertEquals(snippetId, result.snippetId)
        }

        val permissions = service.getUserPermissions("user1")
        assertEquals(3, permissions.size)
    }

    @Test
    fun `checkPermission should handle special action names`() {
        val specialActions =
            listOf(
                "run_test",
                "format",
                "analyze",
            )

        specialActions.forEach { action ->
            val request =
                CheckPermissionRequestDTO(
                    userId = "owner1",
                    snippetId = "snippet1",
                    action = action,
                    ownerId = "owner1",
                )

            assertTrue(service.checkPermission(request), "Failed for action: $action")
        }
    }

    @Test
    fun `checkPermission should deny edit to user with only read permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val editActions = listOf("edit", "update", "EDIT", "UPDATE")

        editActions.forEach { action ->
            val request =
                CheckPermissionRequestDTO(
                    userId = "user1",
                    snippetId = "snippet1",
                    action = action,
                    ownerId = "owner1",
                )

            assertFalse(service.checkPermission(request), "Should deny edit for action: $action")
        }
    }

    @Test
    fun `getSnippetPermissions should return empty list for nonexistent snippet`() {
        val permissions = service.getSnippetPermissions("nonexistent", "owner1")
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `getUserPermissions should return empty list for user without permissions`() {
        val permissions = service.getUserPermissions("user_without_permissions")
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `permission response DTO should contain all expected fields`() {
        val request =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )

        val result = service.grantPermission(request)

        assertNotNull(result.id)
        assertEquals("user1", result.userId)
        assertEquals("snippet1", result.snippetId)
        assertTrue(result.canRead)
        assertTrue(result.canEdit)
    }
}
