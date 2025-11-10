package api.auth0

import api.users.config.Auth0Config
import api.dtos.responses.Auth0UserResponseDTO
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class DefaultAuth0UserRepository(
    private val restTemplate: RestTemplate,
    private val auth0Config: Auth0Config,
    private val tokenManager: Auth0TokenManager,
) : Auth0UserRepository {

    override fun getUsers(
        query: String?,
        page: Int,
        pageSize: Int,
        searchEngine: String,
    ): List<Auth0UserResponseDTO> {
        val token = tokenManager.getManagementApiToken()

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
        val headers = createHeaders(token)
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

    override fun getUserById(userId: String): Auth0UserResponseDTO {
        val token = tokenManager.getManagementApiToken()
        val url = "https://${auth0Config.domain}/api/v2/users/$userId"
        val headers = createHeaders(token)
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

    override fun getUsersByEmail(email: String): List<Auth0UserResponseDTO> =
        getUsers(query = "email:\"$email\"", page = 0, pageSize = 50, searchEngine = "v3")

    private fun createHeaders(token: String): HttpHeaders =
        HttpHeaders().apply {
            set("Authorization", "Bearer $token")
            accept = listOf(MediaType.APPLICATION_JSON)
        }
}
