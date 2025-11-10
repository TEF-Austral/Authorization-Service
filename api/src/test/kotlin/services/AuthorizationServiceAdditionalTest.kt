package services

import api.dtos.requests.CheckPermissionRequestDTO
import api.dtos.requests.GrantPermissionRequestDTO
import api.services.AuthorizationService
import api.services.authorization.DefaultPermissionChecker
import api.services.authorization.DefaultPermissionManager
import api.services.authorization.DefaultPermissionQueryService
import api.services.authorization.PermissionMapper
import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import repositories.MockPermissionRepository

class AuthorizationServiceAdditionalTest {

    private lateinit var repository: MockPermissionRepository
    private lateinit var service: AuthorizationService

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
        val permissionChecker = DefaultPermissionChecker(repository)
        val permissionMapper = PermissionMapper()
        val permissionManager = DefaultPermissionManager(repository, permissionMapper)
        val permissionQueryService = DefaultPermissionQueryService(repository, permissionMapper)
        service = AuthorizationService(permissionChecker, permissionManager, permissionQueryService)
    }

    @Test
    fun `checkPermission should handle all action variations`() {
        val actions =
            mapOf(
                "create" to true,
                "CREATE" to true,
                "Create" to true,
                "read" to false,
                "READ" to false,
                "edit" to false,
                "EDIT" to false,
                "delete" to false,
                "DELETE" to false,
                "share" to false,
                "SHARE" to false,
                "execute" to false,
                "EXECUTE" to false,
                "format" to false,
                "FORMAT" to false,
                "analyze" to false,
                "ANALYZE" to false,
                "run_test" to false,
                "RUN_TEST" to false,
                "grant_permission" to false,
                "GRANT_PERMISSION" to false,
                "unknown" to false,
                "invalid_action" to false,
            )

        actions.forEach { (action, expected) ->
            val request =
                CheckPermissionRequestDTO(
                    userId = "user1",
                    snippetId = "snippet1",
                    action = action,
                    ownerId = "owner1",
                )
            val result = service.checkPermission(request)
            assertEquals(expected, result, "Action $action should be $expected")
        }
    }

    @Test
    fun `revokePermission should handle multiple revocations`() {
        repository.save(
            Permission(userId = "user1", snippetId = "snippet1", canRead = true, canEdit = false),
        )

        service.revokePermission("user1", "snippet1", "owner1")

        assertThrows<IllegalArgumentException> {
            service.revokePermission("user1", "snippet1", "owner1")
        }
    }

    @Test
    fun `getSnippetPermissions should handle large result sets`() {
        repeat(100) { i ->
            repository.save(
                Permission(
                    userId = "user$i",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit =
                        i % 2 == 0,
                ),
            )
        }

        val permissions = service.getSnippetPermissions("snippet1", "owner1")
        assertEquals(100, permissions.size)
    }

    @Test
    fun `getUserPermissions should handle large result sets`() {
        repeat(100) { i ->
            repository.save(
                Permission(
                    userId = "user1",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit =
                        i % 2 == 0,
                ),
            )
        }

        val permissions = service.getUserPermissions("user1")
        assertEquals(100, permissions.size)
    }

    @Test
    fun `grantPermission should validate requester and owner match`() {
        val request =
            GrantPermissionRequestDTO(
                requesterId = "user1",
                ownerId = "owner1",
                granteeId = "user2",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val exception =
            assertThrows<SecurityException> {
                service.grantPermission(request)
            }
        assertEquals("Only the owner can grant permissions", exception.message)
    }

    @Test
    fun `grantPermission should prevent owner from granting to self`() {
        val request =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "owner1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val exception =
            assertThrows<IllegalArgumentException> {
                service.grantPermission(request)
            }
        assertEquals("Cannot grant permissions to the owner", exception.message)
    }

    @Test
    fun `checkPermission should allow owner all actions except unknown`() {
        val allowedActions =
            listOf(
                "read",
                "edit",
                "update",
                "delete",
                "share",
                "grant_permission",
                "execute",
                "run_test",
                "format",
                "analyze",
            )

        allowedActions.forEach { action ->
            val request =
                CheckPermissionRequestDTO(
                    userId = "owner1",
                    snippetId = "snippet1",
                    action = action,
                    ownerId = "owner1",
                )
            assertTrue(service.checkPermission(request), "Owner should be allowed $action")
        }
    }

    @Test
    fun `checkPermission should deny non-owner special actions even with full permissions`() {
        repository.save(
            Permission(userId = "user1", snippetId = "snippet1", canRead = true, canEdit = true),
        )

        val deniedActions = listOf("delete", "share", "grant_permission")

        deniedActions.forEach { action ->
            val request =
                CheckPermissionRequestDTO(
                    userId = "user1",
                    snippetId = "snippet1",
                    action = action,
                    ownerId = "owner1",
                )
            assertFalse(service.checkPermission(request), "Non-owner should be denied $action")
        }
    }

    @Test
    fun `grantPermission should handle permission downgrades`() {
        repository.save(
            Permission(userId = "user1", snippetId = "snippet1", canRead = true, canEdit = true),
        )

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
    fun `revokePermission should require permission to exist`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                service.revokePermission("user1", "snippet1", "owner1")
            }
        assertEquals("Permission not found", exception.message)
    }

    @Test
    fun `getSnippetPermissions should return empty list for new snippet`() {
        val permissions = service.getSnippetPermissions("new_snippet", "owner1")
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `getUserPermissions should return empty list for new user`() {
        val permissions = service.getUserPermissions("new_user")
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `checkPermission should handle whitespace in action`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = " read ",
                ownerId = "owner1",
            )
        assertFalse(service.checkPermission(request))
    }

    @Test
    fun `grantPermission should create permission with both flags false`() {
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
}
