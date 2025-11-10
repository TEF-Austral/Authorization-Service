package integration

import api.controllers.AuthorizationController
import api.dtos.requests.GetSnippetPermissionsRequestDTO
import api.dtos.requests.CheckPermissionRequestDTO
import api.dtos.requests.GrantPermissionRequestDTO
import api.dtos.requests.RevokePermissionRequestDTO
import api.services.AuthorizationService
import api.services.authorization.DefaultPermissionChecker
import api.services.authorization.DefaultPermissionManager
import api.services.authorization.DefaultPermissionQueryService
import api.services.authorization.PermissionMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import repositories.MockPermissionRepository

class AuthorizationIntegrationTest {

    private lateinit var repository: MockPermissionRepository
    private lateinit var service: AuthorizationService
    private lateinit var controller: AuthorizationController

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
        val permissionChecker = DefaultPermissionChecker(repository)
        val permissionMapper = PermissionMapper()
        val permissionManager = DefaultPermissionManager(repository, permissionMapper)
        val permissionQueryService = DefaultPermissionQueryService(repository, permissionMapper)
        service = AuthorizationService(permissionChecker, permissionManager, permissionQueryService)
        controller = AuthorizationController(service)
    }

    @Test
    fun `full workflow - owner grants read permission and user can read`() {
        val checkRequest1 =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )
        val checkResponse1 = controller.checkPermission(checkRequest1)
        assertFalse(checkResponse1.body!!.allowed)

        val grantRequest =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )
        val grantResponse = controller.grantPermission(grantRequest)
        assertTrue(grantResponse.body!!.canRead)
        assertFalse(grantResponse.body!!.canEdit)

        val checkRequest2 =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )
        val checkResponse2 = controller.checkPermission(checkRequest2)
        assertTrue(checkResponse2.body!!.allowed)

        val checkRequest3 =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )
        val checkResponse3 = controller.checkPermission(checkRequest3)
        assertFalse(checkResponse3.body!!.allowed)
    }

    @Test
    fun `full workflow - owner upgrades user from read to edit`() {
        val grantReadRequest =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )
        controller.grantPermission(grantReadRequest)

        val checkRead =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )
        assertTrue(controller.checkPermission(checkRead).body!!.allowed)

        val checkEdit =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )
        assertFalse(controller.checkPermission(checkEdit).body!!.allowed)

        val grantEditRequest =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )
        controller.grantPermission(grantEditRequest)

        assertTrue(controller.checkPermission(checkEdit).body!!.allowed)
    }

    @Test
    fun `full workflow - owner revokes permission`() {
        val grantRequest =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            )
        controller.grantPermission(grantRequest)

        val checkRead =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )
        assertTrue(controller.checkPermission(checkRead).body!!.allowed)

        val checkEdit =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )
        assertTrue(controller.checkPermission(checkEdit).body!!.allowed)

        val revokeRequest =
            RevokePermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                requesterId = "owner1",
            )
        controller.revokePermission(revokeRequest)

        assertFalse(controller.checkPermission(checkRead).body!!.allowed)
        assertFalse(controller.checkPermission(checkEdit).body!!.allowed)
    }

    @Test
    fun `full workflow - multiple users with different permissions`() {
        controller.grantPermission(
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        controller.grantPermission(
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user2",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            ),
        )

        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "read", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
        assertFalse(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "edit", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )

        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user2", "read", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user2", "edit", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )

        val snippetPermissions =
            controller.getSnippetPermissions(
                GetSnippetPermissionsRequestDTO(
                    requesterId = "owner1",
                    snippetId = "snippet1",
                ),
            )
        assertEquals(2, snippetPermissions.body!!.size)
    }

    @Test
    fun `full workflow - user with permissions across multiple snippets`() {
        controller.grantPermission(
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        controller.grantPermission(
            GrantPermissionRequestDTO(
                requesterId = "owner2",
                ownerId = "owner2",
                granteeId = "user1",
                snippetId = "snippet2",
                canRead = true,
                canEdit = true,
            ),
        )

        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "read", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
        assertFalse(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "edit", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "read", "snippet2", "owner2"),
                ).body!!
                .allowed,
        )
        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "edit", "snippet2", "owner2"),
                ).body!!
                .allowed,
        )

        val userPermissions = controller.getUserPermissions("user1")
        assertEquals(2, userPermissions.body!!.size)
    }

    @Test
    fun `full workflow - owner permissions are implicit not explicit`() {
        val actions = listOf("read", "edit", "delete", "share", "execute")

        actions.forEach { action ->
            assertTrue(
                controller
                    .checkPermission(
                        CheckPermissionRequestDTO("owner1", action, "snippet1", "owner1"),
                    ).body!!
                    .allowed,
                "Owner should be allowed to $action",
            )
        }

        val ownerPermissions = controller.getUserPermissions("owner1")
        assertTrue(ownerPermissions.body!!.isEmpty())
    }

    @Test
    fun `full workflow - security checks prevent unauthorized grants`() {
        val unauthorizedGrant =
            GrantPermissionRequestDTO(
                requesterId = "user1",
                ownerId = "owner1",
                granteeId = "user2",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertThrows<SecurityException> {
            controller.grantPermission(unauthorizedGrant)
        }

        val permissions =
            controller.getSnippetPermissions(
                GetSnippetPermissionsRequestDTO(
                    requesterId = "owner1",
                    snippetId = "snippet1",
                ),
            )
        assertTrue(permissions.body!!.isEmpty())
    }

    @Test
    fun `full workflow - execute action requires read permission`() {
        assertFalse(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "execute", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )

        controller.grantPermission(
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "execute", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
    }

    @Test
    fun `full workflow - special actions format and analyze require read`() {
        controller.grantPermission(
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "format", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "analyze", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
        assertTrue(
            controller
                .checkPermission(
                    CheckPermissionRequestDTO("user1", "run_test", "snippet1", "owner1"),
                ).body!!
                .allowed,
        )
    }

    @Test
    fun `full workflow - anyone can create snippets`() {
        val users = listOf("user1", "user2", "user3", "owner1", "random_user")

        users.forEach { userId ->
            assertTrue(
                controller
                    .checkPermission(
                        CheckPermissionRequestDTO(userId, "create", "new_snippet", "owner1"),
                    ).body!!
                    .allowed,
                "User $userId should be able to create",
            )
        }
    }

    @Test
    fun `full workflow - owner exclusive actions`() {
        val exclusiveActions = listOf("delete", "share", "grant_permission")

        controller.grantPermission(
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = true,
            ),
        )

        exclusiveActions.forEach { action ->
            assertFalse(
                controller
                    .checkPermission(
                        CheckPermissionRequestDTO("user1", action, "snippet1", "owner1"),
                    ).body!!
                    .allowed,
                "User should not be allowed to $action",
            )

            assertTrue(
                controller
                    .checkPermission(
                        CheckPermissionRequestDTO("owner1", action, "snippet1", "owner1"),
                    ).body!!
                    .allowed,
                "Owner should be allowed to $action",
            )
        }
    }
}
