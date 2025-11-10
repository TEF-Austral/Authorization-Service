package api.auth0

import api.users.config.Auth0Config
import api.dtos.responses.Auth0TokenResponseDTO
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class DefaultAuth0TokenManager(
    private val restTemplate: RestTemplate,
    private val auth0Config: Auth0Config,
) : Auth0TokenManager {

    private var cachedToken: String? = null
    private var tokenExpirationTime: Long = 0

    override fun getManagementApiToken(): String {
        val currentTime = System.currentTimeMillis() / 1000

        if (cachedToken != null && currentTime < tokenExpirationTime) {
            return cachedToken!!
        }

        return fetchNewToken(currentTime)
    }

    private fun fetchNewToken(currentTime: Long): String {
        val url = "https://${auth0Config.domain}/oauth/token"
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val body =
            mapOf(
                "client_id" to auth0Config.management.clientId,
                "client_secret" to auth0Config.management.clientSecret,
                "audience" to "https://${auth0Config.domain}/api/v2/",
                "grant_type" to "client_credentials",
            )

        val request = HttpEntity(body, headers)
        val response =
            restTemplate.postForObject(url, request, Auth0TokenResponseDTO::class.java)
                ?: throw RuntimeException("Failed to obtain Auth0 Management API token")

        cachedToken = response.accessToken
        tokenExpirationTime = currentTime + response.expiresIn - 60

        return cachedToken!!
    }
}
