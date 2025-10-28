package controllers

import api.dtos.GetSnippetPermissionsRequestDTO
import dtos.CheckPermissionRequestDTO
import dtos.GrantPermissionRequestDTO
import dtos.RevokePermissionRequestDTO
import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import repositories.MockPermissionRepository
import services.AuthorizationService

class AuthorizationControllerTest {

    private lateinit var repository: MockPermissionRepository
    private lateinit var service: AuthorizationService
    private lateinit var controller: AuthorizationController

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
        service = AuthorizationService(repository)
        controller = AuthorizationController(service)
    }

    @Test
    fun `checkPermission should return 200 with allowed true for create action`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "create",
                ownerId = "owner1",
            )

        val response = controller.checkPermission(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertTrue(response.body!!.allowed)
    }

    @Test
    fun `checkPermission should return 200 with allowed false for unauthorized read`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        val response = controller.checkPermission(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertFalse(response.body!!.allowed)
    }

    @Test
    fun `checkPermission should return 200 with allowed true for owner read`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        val response = controller.checkPermission(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertTrue(response.body!!.allowed)
    }

    @Test
    fun `grantPermission should return 200 with permission details`() {
        val request =
            GrantPermissionRequestDTO(
                requesterId = "owner1",
                ownerId = "owner1",
                granteeId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val response = controller.grantPermission(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals("user1", response.body!!.userId)
        assertEquals("snippet1", response.body!!.snippetId)
        assertTrue(response.body!!.canRead)
        assertFalse(response.body!!.canEdit)
    }

    @Test
    fun `grantPermission should throw SecurityException for non-owner requester`() {
        val request =
            GrantPermissionRequestDTO(
                requesterId = "user1",
                ownerId = "owner1",
                granteeId = "user2",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        assertThrows<SecurityException> {
            controller.grantPermission(request)
        }
    }

    @Test
    fun `revokePermission should return 204 on success`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        val request =
            RevokePermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                requesterId = "owner1",
            )

        val response = controller.revokePermission(request)

        assertEquals(204, response.statusCode.value())
    }

    @Test
    fun `revokePermission should throw exception when permission not found`() {
        val request =
            RevokePermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                requesterId = "owner1",
            )

        assertThrows<IllegalArgumentException> {
            controller.revokePermission(request)
        }
    }

    @Test
    fun `getSnippetPermissions should return empty list when no permissions`() {
        val request =
            GetSnippetPermissionsRequestDTO(
                requesterId = "owner1",
                snippetId = "snippet1",
            )

        val response = controller.getSnippetPermissions(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertTrue(response.body!!.isEmpty())
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

        val request =
            GetSnippetPermissionsRequestDTO(
                requesterId = "owner1",
                snippetId = "snippet1",
            )

        val response = controller.getSnippetPermissions(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(2, response.body!!.size)
        assertTrue(response.body!!.any { it.userId == "user1" })
        assertTrue(response.body!!.any { it.userId == "user2" })
    }

    @Test
    fun `getUserPermissions should return empty list when no permissions`() {
        val response = controller.getUserPermissions("user1")

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertTrue(response.body!!.isEmpty())
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

        val response = controller.getUserPermissions("user1")

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(2, response.body!!.size)
        assertTrue(response.body!!.any { it.snippetId == "snippet1" })
        assertTrue(response.body!!.any { it.snippetId == "snippet2" })
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

        val response = controller.grantPermission(request)

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertTrue(response.body!!.canRead)
        assertTrue(response.body!!.canEdit)
    }

    @Test
    fun `checkPermission should handle multiple different actions`() {
        val actions = listOf("read", "edit", "delete", "share", "execute", "format", "analyze")

        actions.forEach { action ->
            val request =
                CheckPermissionRequestDTO(
                    userId = "owner1",
                    snippetId = "snippet1",
                    action = action,
                    ownerId = "owner1",
                )

            val response = controller.checkPermission(request)

            assertEquals(200, response.statusCode.value())
            assertNotNull(response.body)
        }
    }
}
