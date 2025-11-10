package services.authorization

import api.dtos.requests.GrantPermissionRequestDTO
import api.services.authorization.DefaultPermissionManager
import api.services.authorization.PermissionMapper
import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.MockPermissionRepository

class DefaultPermissionManagerTest {

    private lateinit var repository: MockPermissionRepository
    private lateinit var mapper: PermissionMapper
    private lateinit var manager: DefaultPermissionManager

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
        mapper = PermissionMapper()
        manager = DefaultPermissionManager(repository, mapper)
    }

    @Test
    fun `grant should create new permission when none exists`() {
        val request =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = true,
                canEdit = false,
            )

        val result = manager.grant(request)

        assertNotNull(result)
        assertEquals("user1", result.userId)
        assertEquals("snippet1", result.snippetId)
        assertTrue(result.canRead)
        assertEquals(false, result.canEdit)
    }

    @Test
    fun `grant should update existing permission`() {
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
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = true,
                canEdit = true,
            )

        val result = manager.grant(request)

        assertNotNull(result)
        assertTrue(result.canRead)
        assertTrue(result.canEdit)
    }

    @Test
    fun `grant should throw SecurityException when requester is not owner`() {
        val request =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "user2",
                canRead = true,
                canEdit = false,
            )

        assertThrows(SecurityException::class.java) {
            manager.grant(request)
        }
    }

    @Test
    fun `grant should throw IllegalArgumentException when granting to owner`() {
        val request =
            GrantPermissionRequestDTO(
                granteeId = "owner1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = true,
                canEdit = false,
            )

        assertThrows(IllegalArgumentException::class.java) {
            manager.grant(request)
        }
    }

    @Test
    fun `grant should allow granting only read permission`() {
        val request =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = true,
                canEdit = false,
            )

        val result = manager.grant(request)

        assertTrue(result.canRead)
        assertEquals(false, result.canEdit)
    }

    @Test
    fun `grant should allow granting only edit permission`() {
        val request =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = false,
                canEdit = true,
            )

        val result = manager.grant(request)

        assertEquals(false, result.canRead)
        assertTrue(result.canEdit)
    }

    @Test
    fun `grant should allow granting both read and edit permission`() {
        val request =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = true,
                canEdit = true,
            )

        val result = manager.grant(request)

        assertTrue(result.canRead)
        assertTrue(result.canEdit)
    }

    @Test
    fun `grant should allow removing all permissions`() {
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
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = false,
                canEdit = false,
            )

        val result = manager.grant(request)

        assertEquals(false, result.canRead)
        assertEquals(false, result.canEdit)
    }

    @Test
    fun `revoke should delete existing permission`() {
        repository.save(
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            ),
        )

        manager.revoke("user1", "snippet1", "owner1")

        val found = repository.findByUserIdAndSnippetId("user1", "snippet1")
        assertEquals(null, found)
    }

    @Test
    fun `revoke should throw IllegalArgumentException when permission does not exist`() {
        assertThrows(IllegalArgumentException::class.java) {
            manager.revoke("user1", "snippet1", "owner1")
        }
    }

    @Test
    fun `grant should preserve permission id when updating`() {
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
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = false,
                canEdit = true,
            )

        val result = manager.grant(request)

        assertEquals(existing.id, result.id)
    }

    @Test
    fun `grant should handle multiple different users for same snippet`() {
        val request1 =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = true,
                canEdit = false,
            )

        val request2 =
            GrantPermissionRequestDTO(
                granteeId = "user2",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = false,
                canEdit = true,
            )

        val result1 = manager.grant(request1)
        val result2 = manager.grant(request2)

        assertEquals("user1", result1.userId)
        assertEquals("user2", result2.userId)
        assertTrue(result1.canRead)
        assertEquals(false, result1.canEdit)
        assertEquals(false, result2.canRead)
        assertTrue(result2.canEdit)
    }

    @Test
    fun `grant should handle same user for multiple snippets`() {
        val request1 =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet1",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = true,
                canEdit = false,
            )

        val request2 =
            GrantPermissionRequestDTO(
                granteeId = "user1",
                snippetId = "snippet2",
                ownerId = "owner1",
                requesterId = "owner1",
                canRead = false,
                canEdit = true,
            )

        val result1 = manager.grant(request1)
        val result2 = manager.grant(request2)

        assertEquals("snippet1", result1.snippetId)
        assertEquals("snippet2", result2.snippetId)
    }
}
