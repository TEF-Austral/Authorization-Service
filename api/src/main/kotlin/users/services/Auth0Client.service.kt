package api.users.services

import api.users.config.Auth0Config
import api.users.dtos.Auth0TokenResponseDTO
import api.users.dtos.Auth0UserResponseDTO
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class Auth0ClientService(
    private val restTemplate: RestTemplate,
    private val auth0Config: Auth0Config,
) {
    private var cachedToken: String? = null
    private var tokenExpirationTime: Long = 0

    fun getManagementApiToken(): String {
        val currentTime = System.currentTimeMillis() / 1000

        if (cachedToken != null && currentTime < tokenExpirationTime) {
            return cachedToken!!
        }

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

    fun getUsers(
        query: String? = null,
        page: Int = 0,
        pageSize: Int = 50,
        searchEngine: String = "v3",
    ): List<Auth0UserResponseDTO> {
        val token = getManagementApiToken()

        val uriBuilder =
            UriComponentsBuilder
                .fromUriString("https://${auth0Config.domain}/api/v2/users")
                .queryParam("page", page)
                .queryParam("per_page", pageSize)
                .queryParam("search_engine", searchEngine)

        if (!query.isNullOrBlank()) {
            uriBuilder.queryParam("q", query)
        }

        val url = uriBuilder.build().toUriString()

        val headers =
            HttpHeaders().apply {
                set("Authorization", "Bearer $token")
                accept = listOf(MediaType.APPLICATION_JSON)
            }

        val request = HttpEntity<Void>(headers)
        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Array<Auth0UserResponseDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun getUserById(userId: String): Auth0UserResponseDTO {
        val token = getManagementApiToken()

        val url = "https://${auth0Config.domain}/api/v2/users/$userId"

        val headers =
            HttpHeaders().apply {
                set("Authorization", "Bearer $token")
                accept = listOf(MediaType.APPLICATION_JSON)
            }

        val request = HttpEntity<Void>(headers)
        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Auth0UserResponseDTO::class.java,
            )

        return response.body ?: throw RuntimeException("User not found: $userId")
    }

    fun getUsersByEmail(email: String): List<Auth0UserResponseDTO> =
        getUsers(query = "email:\"$email\"")
}
