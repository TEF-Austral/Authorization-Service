package repositories

import entities.Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DefaultPermissionRepositoryTest {

    private lateinit var jpaRepository: JpaPermissionRepository
    private lateinit var repository: DefaultPermissionRepository

    @BeforeEach
    fun setUp() {
        jpaRepository = mock()
        repository = DefaultPermissionRepository(jpaRepository)
    }

    @Test
    fun `findByUserIdAndSnippetId should delegate to JPA repository`() {
        val permission =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        whenever(jpaRepository.findByUserIdAndSnippetId("user1", "snippet1"))
            .thenReturn(permission)

        val result = repository.findByUserIdAndSnippetId("user1", "snippet1")

        assertEquals(permission, result)
        verify(jpaRepository).findByUserIdAndSnippetId("user1", "snippet1")
    }

    @Test
    fun `findByUserIdAndSnippetId should return null when not found`() {
        whenever(jpaRepository.findByUserIdAndSnippetId("user1", "snippet1"))
            .thenReturn(null)

        val result = repository.findByUserIdAndSnippetId("user1", "snippet1")

        assertNull(result)
        verify(jpaRepository).findByUserIdAndSnippetId("user1", "snippet1")
    }

    @Test
    fun `save should delegate to JPA repository`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val savedPermission = permission.copy(id = 1L)

        whenever(jpaRepository.save(permission))
            .thenReturn(savedPermission)

        val result = repository.save(permission)

        assertEquals(savedPermission, result)
        assertEquals(1L, result.id)
        verify(jpaRepository).save(permission)
    }

    @Test
    fun `save should update existing permission`() {
        val existingPermission =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val updatedPermission = existingPermission.copy(canEdit = true)

        whenever(jpaRepository.save(updatedPermission))
            .thenReturn(updatedPermission)

        val result = repository.save(updatedPermission)

        assertEquals(updatedPermission, result)
        assertEquals(true, result.canEdit)
        verify(jpaRepository).save(updatedPermission)
    }

    @Test
    fun `deleteByUserIdAndSnippetId should delegate to JPA repository`() {
        repository.deleteByUserIdAndSnippetId("user1", "snippet1")

        verify(jpaRepository).deleteByUserIdAndSnippetId("user1", "snippet1")
    }

    @Test
    fun `deleteByUserIdAndSnippetId should handle non-existent permission`() {
        doNothing().whenever(jpaRepository).deleteByUserIdAndSnippetId("user1", "snippet1")

        repository.deleteByUserIdAndSnippetId("user1", "snippet1")

        verify(jpaRepository).deleteByUserIdAndSnippetId("user1", "snippet1")
    }

    @Test
    fun `findAllBySnippetId should delegate to JPA repository`() {
        val permissions =
            listOf(
                Permission(
                    id = 1L,
                    userId = "user1",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = false,
                ),
                Permission(
                    id = 2L,
                    userId = "user2",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = true,
                ),
            )

        whenever(jpaRepository.findAllBySnippetId("snippet1"))
            .thenReturn(permissions)

        val result = repository.findAllBySnippetId("snippet1")

        assertEquals(2, result.size)
        assertEquals(permissions, result)
        verify(jpaRepository).findAllBySnippetId("snippet1")
    }

    @Test
    fun `findAllBySnippetId should return empty list when no permissions exist`() {
        whenever(jpaRepository.findAllBySnippetId("snippet1"))
            .thenReturn(emptyList())

        val result = repository.findAllBySnippetId("snippet1")

        assertEquals(0, result.size)
        verify(jpaRepository).findAllBySnippetId("snippet1")
    }

    @Test
    fun `findAllByUserId should delegate to JPA repository`() {
        val permissions =
            listOf(
                Permission(
                    id = 1L,
                    userId = "user1",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = false,
                ),
                Permission(
                    id = 2L,
                    userId = "user1",
                    snippetId = "snippet2",
                    canRead = true,
                    canEdit = true,
                ),
            )

        whenever(jpaRepository.findAllByUserId("user1"))
            .thenReturn(permissions)

        val result = repository.findAllByUserId("user1")

        assertEquals(2, result.size)
        assertEquals(permissions, result)
        verify(jpaRepository).findAllByUserId("user1")
    }

    @Test
    fun `findAllByUserId should return empty list when no permissions exist`() {
        whenever(jpaRepository.findAllByUserId("user1"))
            .thenReturn(emptyList())

        val result = repository.findAllByUserId("user1")

        assertEquals(0, result.size)
        verify(jpaRepository).findAllByUserId("user1")
    }

    @Test
    fun `save should handle permission with all fields`() {
        val permission =
            Permission(
                id = null,
                userId = "user123",
                snippetId = "snippet456",
                canRead = true,
                canEdit = true,
            )

        val savedPermission = permission.copy(id = 5L)

        whenever(jpaRepository.save(permission))
            .thenReturn(savedPermission)

        val result = repository.save(permission)

        assertEquals(5L, result.id)
        assertEquals("user123", result.userId)
        assertEquals("snippet456", result.snippetId)
        assertEquals(true, result.canRead)
        assertEquals(true, result.canEdit)
    }

    @Test
    fun `findByUserIdAndSnippetId should handle different user-snippet combinations`() {
        val permission1 =
            Permission(
                id = 1L,
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val permission2 =
            Permission(
                id = 2L,
                userId = "user2",
                snippetId = "snippet2",
                canRead = false,
                canEdit = true,
            )

        whenever(jpaRepository.findByUserIdAndSnippetId("user1", "snippet1"))
            .thenReturn(permission1)
        whenever(jpaRepository.findByUserIdAndSnippetId("user2", "snippet2"))
            .thenReturn(permission2)

        val result1 = repository.findByUserIdAndSnippetId("user1", "snippet1")
        val result2 = repository.findByUserIdAndSnippetId("user2", "snippet2")

        assertEquals(permission1, result1)
        assertEquals(permission2, result2)
    }

    @Test
    fun `deleteByUserIdAndSnippetId should handle multiple deletes`() {
        doNothing().whenever(jpaRepository).deleteByUserIdAndSnippetId(any(), any())

        repository.deleteByUserIdAndSnippetId("user1", "snippet1")
        repository.deleteByUserIdAndSnippetId("user2", "snippet2")
        repository.deleteByUserIdAndSnippetId("user3", "snippet3")

        verify(jpaRepository, times(3)).deleteByUserIdAndSnippetId(any(), any())
    }

    @Test
    fun `findAllBySnippetId should handle large result sets`() {
        val permissions =
            (1..100).map { i ->
                Permission(
                    id = i.toLong(),
                    userId = "user$i",
                    snippetId = "snippet1",
                    canRead = true,
                    canEdit = i % 2 == 0,
                )
            }

        whenever(jpaRepository.findAllBySnippetId("snippet1"))
            .thenReturn(permissions)

        val result = repository.findAllBySnippetId("snippet1")

        assertEquals(100, result.size)
    }

    @Test
    fun `findAllByUserId should handle large result sets`() {
        val permissions =
            (1..100).map { i ->
                Permission(
                    id = i.toLong(),
                    userId = "user1",
                    snippetId = "snippet$i",
                    canRead = true,
                    canEdit = i % 2 == 0,
                )
            }

        whenever(jpaRepository.findAllByUserId("user1"))
            .thenReturn(permissions)

        val result = repository.findAllByUserId("user1")
    }

    @Test
    fun `repository should maintain transactional behavior for delete`() {
        doNothing().whenever(jpaRepository).deleteByUserIdAndSnippetId("user1", "snippet1")

        repository.deleteByUserIdAndSnippetId("user1", "snippet1")

        verify(jpaRepository).deleteByUserIdAndSnippetId("user1", "snippet1")
    }

    @Test
    fun `save should handle permission with only read access`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = true,
                canEdit = false,
            )

        val savedPermission = permission.copy(id = 1L)

        whenever(jpaRepository.save(permission))
            .thenReturn(savedPermission)

        val result = repository.save(permission)

        assertEquals(true, result.canRead)
        assertEquals(false, result.canEdit)
    }

    @Test
    fun `save should handle permission with only edit access`() {
        val permission =
            Permission(
                userId = "user1",
                snippetId = "snippet1",
                canRead = false,
                canEdit = true,
            )

        val savedPermission = permission.copy(id = 1L)

        whenever(jpaRepository.save(permission))
            .thenReturn(savedPermission)

        val result = repository.save(permission)

        assertEquals(false, result.canRead)
        assertEquals(true, result.canEdit)
    }

    @Test
    fun `findByUserIdAndSnippetId should handle empty strings`() {
        whenever(jpaRepository.findByUserIdAndSnippetId("", ""))
            .thenReturn(null)

        val result = repository.findByUserIdAndSnippetId("", "")

        assertNull(result)
        verify(jpaRepository).findByUserIdAndSnippetId("", "")
    }
}
