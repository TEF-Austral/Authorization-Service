package dtos

import api.users.models.User
import api.dtos.PaginatedUsersDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class PaginatedUsersDTOTest {

    @Test
    fun `should create PaginatedUsersDTO with users`() {
        val users =
            listOf(
                User("user1", "username1", "email1@test.com", "Name 1", "pic1.jpg"),
                User("user2", "username2", "email2@test.com", "Name 2", "pic2.jpg"),
                User("user3", "username3", "email3@test.com", "Name 3", null),
            )

        val dto =
            PaginatedUsersDTO(
                users = users,
                page = 0,
                pageSize = 50,
                total = 3,
            )

        assertNotNull(dto)
        assertEquals(3, dto.users.size)
        assertEquals(0, dto.page)
        assertEquals(50, dto.pageSize)
        assertEquals(3, dto.total)
    }

    @Test
    fun `should create PaginatedUsersDTO with empty user list`() {
        val dto =
            PaginatedUsersDTO(
                users = emptyList(),
                page = 0,
                pageSize = 50,
                total = 0,
            )

        assertNotNull(dto)
        assertEquals(0, dto.users.size)
        assertEquals(0, dto.page)
        assertEquals(50, dto.pageSize)
        assertEquals(0, dto.total)
    }

    @Test
    fun `should create PaginatedUsersDTO with custom page`() {
        val users =
            listOf(
                User("user1", "username1", "email1@test.com", "Name 1", null),
            )

        val dto =
            PaginatedUsersDTO(
                users = users,
                page = 5,
                pageSize = 10,
                total = 1,
            )

        assertNotNull(dto)
        assertEquals(1, dto.users.size)
        assertEquals(5, dto.page)
        assertEquals(10, dto.pageSize)
        assertEquals(1, dto.total)
    }

    @Test
    fun `should create PaginatedUsersDTO with large page size`() {
        val users =
            List(50) { index ->
                User("user$index", "username$index", "email$index@test.com", "Name $index", null)
            }

        val dto =
            PaginatedUsersDTO(
                users = users,
                page = 0,
                pageSize = 50,
                total = 50,
            )

        assertNotNull(dto)
        assertEquals(50, dto.users.size)
        assertEquals(0, dto.page)
        assertEquals(50, dto.pageSize)
        assertEquals(50, dto.total)
    }

    @Test
    fun `should create PaginatedUsersDTO with small page size`() {
        val users =
            listOf(
                User("user1", "username1", "email1@test.com", "Name 1", null),
            )

        val dto =
            PaginatedUsersDTO(
                users = users,
                page = 0,
                pageSize = 1,
                total = 1,
            )

        assertNotNull(dto)
        assertEquals(1, dto.users.size)
        assertEquals(0, dto.page)
        assertEquals(1, dto.pageSize)
        assertEquals(1, dto.total)
    }

    @Test
    fun `should handle users with null optional fields`() {
        val users =
            listOf(
                User("user1", null, null, null, null),
                User("user2", "username2", "email2@test.com", "Name 2", "pic2.jpg"),
            )

        val dto =
            PaginatedUsersDTO(
                users = users,
                page = 0,
                pageSize = 50,
                total = 2,
            )

        assertNotNull(dto)
        assertEquals(2, dto.users.size)
        assertEquals(null, dto.users[0].username)
        assertEquals("username2", dto.users[1].username)
    }

    @Test
    fun `should create PaginatedUsersDTO for last page`() {
        val users =
            listOf(
                User("user1", "username1", "email1@test.com", "Name 1", null),
            )

        val dto =
            PaginatedUsersDTO(
                users = users,
                page = 9,
                pageSize = 10,
                total = 1,
            )

        assertNotNull(dto)
        assertEquals(1, dto.users.size)
        assertEquals(9, dto.page)
        assertEquals(10, dto.pageSize)
    }

    @Test
    fun `should create PaginatedUsersDTO with different total and users size`() {
        val users =
            listOf(
                User("user1", "username1", "email1@test.com", "Name 1", null),
                User("user2", "username2", "email2@test.com", "Name 2", null),
            )

        val dto =
            PaginatedUsersDTO(
                users = users,
                page = 0,
                pageSize = 10,
                total = 25,
            )

        assertNotNull(dto)
        assertEquals(2, dto.users.size)
        assertEquals(25, dto.total)
    }
}
