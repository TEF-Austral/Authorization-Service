package users.models

import api.users.models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun `should create User with all fields`() {
        val user =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = "Test User",
                picture = "https://example.com/pic.jpg",
            )

        assertNotNull(user)
        assertEquals("auth0|12345", user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.name)
        assertEquals("https://example.com/pic.jpg", user.picture)
    }

    @Test
    fun `should create User with null optional fields`() {
        val user =
            User(
                id = "auth0|12345",
                username = null,
                email = null,
                name = null,
                picture = null,
            )

        assertNotNull(user)
        assertEquals("auth0|12345", user.id)
        assertNull(user.username)
        assertNull(user.email)
        assertNull(user.name)
        assertNull(user.picture)
    }

    @Test
    fun `should create User with minimal information`() {
        val user =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = null,
                picture = null,
            )

        assertNotNull(user)
        assertEquals("auth0|12345", user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertNull(user.name)
        assertNull(user.picture)
    }

    @Test
    fun `should create User with different ID formats`() {
        val googleUser =
            User(
                id = "google-oauth2|123456",
                username = "googleuser",
                email = "google@example.com",
                name = "Google User",
                picture = null,
            )

        assertNotNull(googleUser)
        assertEquals("google-oauth2|123456", googleUser.id)
        assertEquals("googleuser", googleUser.username)
    }

    @Test
    fun `should create User with Facebook ID format`() {
        val facebookUser =
            User(
                id = "facebook|987654321",
                username = "fbuser",
                email = "facebook@example.com",
                name = "Facebook User",
                picture = "https://facebook.com/pic.jpg",
            )

        assertNotNull(facebookUser)
        assertEquals("facebook|987654321", facebookUser.id)
        assertEquals("fbuser", facebookUser.username)
        assertEquals("https://facebook.com/pic.jpg", facebookUser.picture)
    }

    @Test
    fun `should create User with Auth0 database ID format`() {
        val auth0User =
            User(
                id = "auth0|5f7c8b9a1c2d3e4f5a6b7c8d",
                username = "auth0user",
                email = "auth0@example.com",
                name = "Auth0 User",
                picture = null,
            )

        assertNotNull(auth0User)
        assertEquals("auth0|5f7c8b9a1c2d3e4f5a6b7c8d", auth0User.id)
        assertEquals("auth0user", auth0User.username)
    }

    @Test
    fun `should handle User with empty ID`() {
        val user =
            User(
                id = "",
                username = "testuser",
                email = "test@example.com",
                name = "Test User",
                picture = null,
            )

        assertNotNull(user)
        assertEquals("", user.id)
        assertEquals("testuser", user.username)
    }

    @Test
    fun `should handle User with special characters in username`() {
        val user =
            User(
                id = "auth0|12345",
                username = "test.user-123",
                email = "test@example.com",
                name = "Test User",
                picture = null,
            )

        assertNotNull(user)
        assertEquals("test.user-123", user.username)
    }

    @Test
    fun `should handle User with special characters in email`() {
        val user =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test+tag@example.com",
                name = "Test User",
                picture = null,
            )

        assertNotNull(user)
        assertEquals("test+tag@example.com", user.email)
    }

    @Test
    fun `should handle User with long name`() {
        val longName = "Very Long Name That Might Contain Multiple Words And Special Characters"

        val user =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = longName,
                picture = null,
            )

        assertNotNull(user)
        assertEquals(longName, user.name)
    }

    @Test
    fun `should handle User with picture URL`() {
        val user =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = "Test User",
                picture = "https://cdn.example.com/avatars/12345.jpg",
            )

        assertNotNull(user)
        assertEquals("https://cdn.example.com/avatars/12345.jpg", user.picture)
    }

    @Test
    fun `User data class should support equality`() {
        val user1 =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = "Test User",
                picture = null,
            )
        val user2 =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = "Test User",
                picture = null,
            )

        assertEquals(user1, user2)
    }

    @Test
    fun `User data class should support copy`() {
        val originalUser =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = "Test User",
                picture = null,
            )

        val copiedUser = originalUser.copy(username = "newusername")

        assertEquals("auth0|12345", copiedUser.id)
        assertEquals("newusername", copiedUser.username)
        assertEquals("test@example.com", copiedUser.email)
    }

    @Test
    fun `User data class should have proper toString`() {
        val user =
            User(
                id = "auth0|12345",
                username = "testuser",
                email = "test@example.com",
                name = "Test User",
                picture = null,
            )

        val toString = user.toString()

        assertNotNull(toString)
        assert(toString.contains("auth0|12345"))
        assert(toString.contains("testuser"))
    }
}
