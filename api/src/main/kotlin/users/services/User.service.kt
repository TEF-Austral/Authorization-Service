package api.users.services

import api.users.dtos.Auth0UserResponseDTO
import api.users.dtos.PaginatedUsersDTO
import api.users.models.User
import org.springframework.stereotype.Service

@Service
class UserService(
    private val auth0ClientService: Auth0ClientService,
) {

    fun getUsers(
        query: String? = null,
        page: Int = 0,
        pageSize: Int = 50,
    ): PaginatedUsersDTO {
        val auth0Users = auth0ClientService.getUsers(query, page, pageSize)
        val users = auth0Users.map { mapToUser(it) }

        return PaginatedUsersDTO(
            users = users,
            page = page,
            pageSize = pageSize,
            total = users.size,
        )
    }

    fun getUserById(userId: String): User {
        val auth0User = auth0ClientService.getUserById(userId)
        return mapToUser(auth0User)
    }

    fun getUsersByEmail(email: String): List<User> {
        val auth0Users = auth0ClientService.getUsersByEmail(email)
        return auth0Users.map { mapToUser(it) }
    }

    fun searchUsers(
        name: String? = null,
        email: String? = null,
        emailVerified: Boolean? = null,
        connection: String? = null,
    ): List<User> {
        val queryParts = mutableListOf<String>()

        name?.let { queryParts.add("name:*$it*") }
        email?.let { queryParts.add("email:\"$it\"") }
        emailVerified?.let { queryParts.add("email_verified:$it") }
        connection?.let { queryParts.add("identities.connection:\"$it\"") }

        val query =
            if (queryParts.isNotEmpty()) {
                queryParts.joinToString(" AND ")
            } else {
                null
            }

        val auth0Users = auth0ClientService.getUsers(query = query)
        return auth0Users.map { mapToUser(it) }
    }

    private fun mapToUser(auth0User: Auth0UserResponseDTO): User =
        User(
            id = auth0User.userId ?: "",
            username = auth0User.username ?: auth0User.nickname,
            email = auth0User.email,
            name = auth0User.name,
            picture = auth0User.picture,
        )
}
