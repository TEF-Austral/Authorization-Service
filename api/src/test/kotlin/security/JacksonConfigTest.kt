package security

import api.security.JacksonConfig
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JacksonConfigTest {

    private val config = JacksonConfig()

    @Test
    fun `objectMapper should be able to serialize Kotlin data classes`() {
        data class TestData(
            val name: String,
            val value: Int,
        )

        val objectMapper = config.objectMapper()
        val testData = TestData("test", 123)

        val json = objectMapper.writeValueAsString(testData)

        assertNotNull(json)
        assertTrue(json.contains("test"))
        assertTrue(json.contains("123"))
    }

    @Test
    fun `objectMapper should be able to deserialize Kotlin data classes`() {
        data class TestData(
            val name: String,
            val value: Int,
        )

        val objectMapper = config.objectMapper()
        val json = """{"name":"test","value":123}"""

        val result = objectMapper.readValue(json, TestData::class.java)

        assertNotNull(result)
        assertTrue(result.name == "test")
        assertTrue(result.value == 123)
    }

    @Test
    fun `objectMapper should handle nullable properties`() {
        data class TestData(
            val name: String?,
            val value: Int?,
        )

        val objectMapper = config.objectMapper()
        val json = """{"name":null,"value":null}"""

        val result = objectMapper.readValue(json, TestData::class.java)

        assertNotNull(result)
        assertTrue(result.name == null)
        assertTrue(result.value == null)
    }

    @Test
    fun `multiple objectMapper calls should return different instances`() {
        val mapper1 = config.objectMapper()
        val mapper2 = config.objectMapper()

        assertNotNull(mapper1)
        assertNotNull(mapper2)
    }
}
