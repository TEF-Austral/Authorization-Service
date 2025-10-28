package services

import api.AuthorizationServiceApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class AuthorizationServiceApplicationUnitTest {

    @Test
    fun `index should return correct version string`() {
        val app = AuthorizationServiceApplication()

        val result = app.index()

        assertEquals("Authorization Service v1.0", result)
    }

    @Test
    fun `health should return map with UP status`() {
        val app = AuthorizationServiceApplication()

        val result = app.health()

        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals("UP", result["status"])
    }

    @Test
    fun `health should always return UP status`() {
        val app = AuthorizationServiceApplication()

        repeat(10) {
            val result = app.health()
            assertEquals("UP", result["status"])
        }
    }

    @Test
    fun `index should return consistent version`() {
        val app = AuthorizationServiceApplication()

        val results = (1..10).map { app.index() }

        results.forEach { result ->
            assertEquals("Authorization Service v1.0", result)
        }
    }
}
