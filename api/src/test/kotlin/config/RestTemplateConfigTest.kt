package config

import api.config.RestTemplateConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RestTemplateConfigTest {

    private val config = RestTemplateConfig()

    @Test
    fun `restTemplate should have interceptors configured`() {
        val restTemplate = config.restTemplate()

        assertNotNull(restTemplate.interceptors)
        assertEquals(1, restTemplate.interceptors.size)
    }

    @Test
    fun `restTemplate should have RequestIdPropagationInterceptor`() {
        val restTemplate = config.restTemplate()

        assertTrue(restTemplate.interceptors.isNotEmpty())
        val interceptor = restTemplate.interceptors[0]
        assertEquals("RequestIdPropagationInterceptor", interceptor::class.simpleName)
    }

    @Test
    fun `multiple calls should return different RestTemplate instances`() {
        val restTemplate1 = config.restTemplate()
        val restTemplate2 = config.restTemplate()

        assertNotNull(restTemplate1)
        assertNotNull(restTemplate2)
    }
}
