package services.authorization

import api.dtos.requests.CheckPermissionRequestDTO
import api.services.authorization.DefaultPermissionChecker
import entities.Permission
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.MockPermissionRepository

class DefaultPermissionCheckerTest {

    private lateinit var repository: MockPermissionRepository
    private lateinit var checker: DefaultPermissionChecker

    @BeforeEach
    fun setUp() {
        repository = MockPermissionRepository()
        checker = DefaultPermissionChecker(repository)
    }

    @Test
    fun `isAllowed should return true for create action regardless of user`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "create",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for CREATE action in uppercase`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "CREATE",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for owner reading own snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return false for non-owner reading without permission`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "read",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for non-owner with read permission`() {
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

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return false for non-owner with only edit permission trying to read`() {
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
                action = "read",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for owner editing own snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should handle update action same as edit`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "update",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return false for non-owner editing without permission`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "edit",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for non-owner with edit permission`() {
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

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for owner deleting own snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "delete",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return false for non-owner deleting snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "delete",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for owner sharing own snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "share",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return false for non-owner sharing snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "share",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return true for owner granting permissions`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "grant_permission",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return false for non-owner granting permissions`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "grant_permission",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow owner to execute snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "execute",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow non-owner with read permission to execute`() {
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

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should deny non-owner without read permission to execute`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "execute",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow owner to run tests`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "run_test",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow non-owner with read permission to run tests`() {
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

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow owner to format snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "format",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow non-owner with read permission to format`() {
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

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow owner to analyze snippet`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "owner1",
                snippetId = "snippet1",
                action = "analyze",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should allow non-owner with read permission to analyze`() {
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

        assertTrue(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should return false for unknown action`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "unknown_action",
                ownerId = "owner1",
            )

        assertFalse(checker.isAllowed(request))
    }

    @Test
    fun `isAllowed should handle mixed case action names`() {
        val request =
            CheckPermissionRequestDTO(
                userId = "user1",
                snippetId = "snippet1",
                action = "CrEaTe",
                ownerId = "owner1",
            )

        assertTrue(checker.isAllowed(request))
    }
}
