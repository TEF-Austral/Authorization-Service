package api

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow

class AuthorizationServiceApplicationTest {

    @Test
    fun `application context should load`() {
        assertDoesNotThrow {
            AuthorizationServiceApplication()
        }
    }

    @Test
    fun `main function should not throw exception`() {
        assertDoesNotThrow {
            val args = arrayOf<String>()
        }
    }
}
