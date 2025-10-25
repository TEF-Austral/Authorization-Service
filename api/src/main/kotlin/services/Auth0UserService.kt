package services

import dtos.CreateUserRequestDTO
import dtos.UserResponseDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.collections.get

@Service
class Auth0UserService(
    private val restTemplate: RestTemplate,
    @Value("\${auth0.domain}") private val domain: String,
    @Value("\${auth0.management.client-id}") private val clientId: String,
    @Value("\${auth0.management.client-secret}") private val clientSecret: String,
) {
    fun createUser(request: CreateUserRequestDTO): UserResponseDTO {
        val token = getManagementToken()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(token)
            }

        val body =
            mapOf(
                "email" to request.email,
                "password" to request.password,
                "name" to request.name,
                "connection" to "Username-Password-Authentication",
            )

        val entity = HttpEntity(body, headers)
        val response =
            restTemplate.exchange(
                "https://$domain/api/v2/users",
                HttpMethod.POST,
                entity,
                Map::class.java,
            )

        val responseBody = response.body as Map<*, *>
        return UserResponseDTO(
            email = responseBody["email"] as String,
            name = responseBody["name"] as String,
            userId = responseBody["user_id"] as String,
        )
    }

    fun deleteUser(userId: String) {
        val token = getManagementToken()

        val headers =
            HttpHeaders().apply {
                setBearerAuth(token)
            }

        val entity = HttpEntity<Void>(headers)

        // Spring RestTemplate automatically encodes the URL, so no manual encoding needed
        restTemplate.exchange(
            "https://$domain/api/v2/users/$userId",
            HttpMethod.DELETE,
            entity,
            Void::class.java,
        )
    }

    private fun getManagementToken(): String {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val body =
            mapOf(
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "audience" to "https://$domain/api/v2/",
                "grant_type" to "client_credentials",
            )

        val entity = HttpEntity(body, headers)
        val response =
            restTemplate.postForEntity(
                "https://$domain/oauth/token",
                entity,
                Map::class.java,
            )

        return response.body?.get("access_token") as String
    }
}
