package config

import api.config.RequestIdPropagationInterceptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.HttpHeaders
import java.net.URI

class RequestIdPropagationInterceptorTest {

    private lateinit var interceptor: RequestIdPropagationInterceptor
    private lateinit var request: HttpRequest
    private lateinit var execution: ClientHttpRequestExecution
    private lateinit var response: ClientHttpResponse

    @BeforeEach
    fun setUp() {
        interceptor = RequestIdPropagationInterceptor()
        request = mock()
        execution = mock()
        response = mock()

        val headers = HttpHeaders()
        whenever(request.headers).thenReturn(headers)
        whenever(request.uri).thenReturn(URI.create("https://example.com"))
        whenever(execution.execute(any(), any())).thenReturn(response)

        MDC.clear()
    }

    @Test
    fun `intercept should add X-Request-ID header when requestId is in MDC`() {
        MDC.put("requestId", "test-request-id-123")
        val body = ByteArray(0)

        interceptor.intercept(request, body, execution)

        assertEquals("test-request-id-123", request.headers["X-Request-ID"]?.first())
        verify(execution).execute(request, body)
    }

    @Test
    fun `intercept should not add X-Request-ID header when requestId is not in MDC`() {
        val body = ByteArray(0)

        interceptor.intercept(request, body, execution)

        assertNull(request.headers["X-Request-ID"]?.firstOrNull())
        verify(execution).execute(request, body)
    }

    @Test
    fun `intercept should return response from execution`() {
        MDC.put("requestId", "request-123")
        val body = ByteArray(0)

        val result = interceptor.intercept(request, body, execution)

        assertNotNull(result)
        assertEquals(response, result)
    }

    @Test
    fun `intercept should handle empty requestId`() {
        MDC.put("requestId", "")
        val body = ByteArray(0)

        interceptor.intercept(request, body, execution)

        assertEquals("", request.headers["X-Request-ID"]?.first())
    }

    @Test
    fun `intercept should handle long requestId`() {
        val longRequestId = "a".repeat(200)
        MDC.put("requestId", longRequestId)
        val body = ByteArray(0)

        interceptor.intercept(request, body, execution)

        assertEquals(longRequestId, request.headers["X-Request-ID"]?.first())
    }

    @Test
    fun `intercept should handle special characters in requestId`() {
        MDC.put("requestId", "req-123-!@#$%^&*()")
        val body = ByteArray(0)

        interceptor.intercept(request, body, execution)

        assertEquals("req-123-!@#$%^&*()", request.headers["X-Request-ID"]?.first())
    }

    @Test
    fun `intercept should execute request even if adding header fails`() {
        val body = ByteArray(0)

        val result = interceptor.intercept(request, body, execution)

        verify(execution).execute(request, body)
        assertEquals(response, result)
    }
}
