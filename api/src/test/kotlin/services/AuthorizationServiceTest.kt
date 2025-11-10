package services

import api.dtos.requests.CheckPermissionRequestDTO
import api.dtos.requests.GrantPermissionRequestDTO
import api.services.AuthorizationService
import api.services.authorization.DefaultPermissionChecker
import api.services.authorization.DefaultPermissionManager
import api.services.authorization.DefaultPermissionQueryService
import api.services.authorization.PermissionMapper
import entities.Permission
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import repositories.MockPermissionRepository

class AuthorizationServiceTest {

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
    fun `checkPermission should allow create action for any user`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "create",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should allow owner to read their own snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should deny read for user without permission`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertFalse(result)
    }

    @Test
    fun `checkPermission should allow read for user with explicit read permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should allow owner to edit their own snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should deny edit for user without permission`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertFalse(result)
    }

    @Test
    fun `checkPermission should allow edit for user with explicit edit permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should deny edit for user with only read permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertFalse(result)
    }

    @Test
    fun `checkPermission should only allow owner to delete`() {
        val ownerRequest =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "delete",
                ownerId = "owner1",
            )

        val userRequest =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "delete",
                ownerId = "owner1",
            )

        Assertions.assertTrue(service.checkPermission(ownerRequest))
        Assertions.assertFalse(service.checkPermission(userRequest))
    }

    @Test
    fun `checkPermission should only allow owner to share`() {
        val ownerRequest =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "share",
                ownerId = "owner1",
            )

        val userRequest =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "share",
                ownerId = "owner1",
            )

        Assertions.assertTrue(service.checkPermission(ownerRequest))
        Assertions.assertFalse(service.checkPermission(userRequest))
    }

    @Test
    fun `checkPermission should allow execute for owner`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "execute",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should allow execute for user with read permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "execute",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should deny unknown action`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "unknown_action",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertFalse(result)
    }

    @Test
    fun `grantPermission should create new permission`() {
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

        assertNotNull(result.id)
        Assertions.assertEquals("user1", result.userId)
        Assertions.assertEquals("snippet1", result.snippetId)
        Assertions.assertTrue(result.canRead)
        Assertions.assertFalse(result.canEdit)
    }

    @Test
    fun `grantPermission should update existing permission`() {
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

        Assertions.assertTrue(result.canRead)
        Assertions.assertTrue(result.canEdit)
    }

    @Test
    fun `grantPermission should throw exception if requester is not owner`() {
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

        Assertions.assertEquals("Only the owner can grant permissions", exception.message)
    }

    @Test
    fun `grantPermission should throw exception if granting to owner`() {
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

        Assertions.assertEquals("Cannot grant permissions to the owner", exception.message)
    }

    @Test
    fun `revokePermission should delete permission`() {
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
        assertNull(permission)
    }

    @Test
    fun `revokePermission should throw exception if permission not found`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                service.revokePermission("user1", "snippet1", "owner1")
            }

        Assertions.assertEquals("Permission not found", exception.message)
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
        repository.save(
            Permission(
                userId = "user3",
                snippetId = "snippet2",
                canRead = true,
                canEdit = false,
            ),
        )

        val permissions = service.getSnippetPermissions("snippet1", "owner1")

        Assertions.assertEquals(2, permissions.size)
        Assertions.assertTrue(permissions.any { it.userId == "user1" })
        Assertions.assertTrue(permissions.any { it.userId == "user2" })
        Assertions.assertFalse(permissions.any { it.userId == "user3" })
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

        val permissions = service.getUserPermissions("user1")

        Assertions.assertEquals(2, permissions.size)
        Assertions.assertTrue(permissions.any { it.snippetId == "snippet1" })
        Assertions.assertTrue(permissions.any { it.snippetId == "snippet2" })
        Assertions.assertFalse(permissions.any { it.snippetId == "snippet3" })
    }

    @Test
    fun `checkPermission should handle update action same as edit`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "update",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should handle run_test action like execute`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "run_test",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should handle format action like execute`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "format",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should handle analyze action like execute`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "analyze",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }

    @Test
    fun `checkPermission should be case insensitive for actions`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "CREATE",
                ownerId = "owner1",
            )

        val result = service.checkPermission(request)

        Assertions.assertTrue(result)
    }
}
