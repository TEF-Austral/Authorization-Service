package api.auth0

import api.dtos.responses.Auth0UserResponseDTO

interface Auth0UserRepository {
    fun getUsers(
        query: String?,
        page: Int,
        pageSize: Int,
        searchEngine: String,
    ): List<Auth0UserResponseDTO>

    fun getUserById(userId: String): Auth0UserResponseDTO

    fun getUsersByEmail(email: String): List<Auth0UserResponseDTO>
}
