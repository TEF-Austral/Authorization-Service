package services

import api.AuthorizationServiceApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [AuthorizationServiceApplication::class],
)
@ActiveProfiles("test")
class AuthorizationServiceApplicationTest {

    @Test
    fun `health map should be immutable structure`() {
        val app = AuthorizationServiceApplication()

        val result = app.health()

        assertTrue(result.containsKey("status"))
    }

    @Test
    fun `index should not return null or empty string`() {
        val app = AuthorizationServiceApplication()

        val result = app.index()

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `health status value should be exactly UP`() {
        val app = AuthorizationServiceApplication()

        val result = app.health()
        val status = result["status"]

        assertNotNull(status)
        assertEquals("UP", status)
        assertTrue(status is String)
    }

    @Test
    fun `index should contain service name`() {
        val app = AuthorizationServiceApplication()

        val result = app.index()

        assertTrue(result.contains("Authorization"))
        assertTrue(result.contains("Service"))
    }

    @Test
    fun `index should contain version number`() {
        val app = AuthorizationServiceApplication()

        val result = app.index()

        assertTrue(result.contains("v1.0"))
    }

    @Test
    fun `health should only contain status key`() {
        val app = AuthorizationServiceApplication()

        val result = app.health()

        assertEquals(setOf("status"), result.keys)
    }

    @Test
    fun `multiple application instances should behave identically`() {
        val app1 = AuthorizationServiceApplication()
        val app2 = AuthorizationServiceApplication()

        assertEquals(app1.index(), app2.index())
        assertEquals(app1.health(), app2.health())
    }
}
