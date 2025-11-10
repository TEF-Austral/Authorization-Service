package api.users.services

import api.dtos.PaginatedUsersDTO
import api.users.models.User
import api.auth0.Auth0UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val auth0UserRepository: Auth0UserRepository,
    private val userMapper: UserMapper,
    private val queryBuilder: UserQueryBuilder,
) {

    fun getUsers(
        query: String? = null,
        page: Int = 0,
        pageSize: Int = 50,
    ): PaginatedUsersDTO {
        val auth0Users = auth0UserRepository.getUsers(query, page, pageSize, "v3")
        val users = auth0Users.map { userMapper.toUser(it) }

        return PaginatedUsersDTO(
            users = users,
            page = page,
            pageSize = pageSize,
            total = users.size,
        )
    }

    fun getUserById(userId: String): User {
        val auth0User = auth0UserRepository.getUserById(userId)
        return userMapper.toUser(auth0User)
    }

    fun getUsersByEmail(email: String): List<User> {
        val auth0Users = auth0UserRepository.getUsersByEmail(email)
        return auth0Users.map { userMapper.toUser(it) }
    }

    fun searchUsers(
        name: String? = null,
        email: String? = null,
        emailVerified: Boolean? = null,
        connection: String? = null,
    ): List<User> {
        val query = queryBuilder.buildSearchQuery(name, email, emailVerified, connection)
        val auth0Users = auth0UserRepository.getUsers(query, 0, 50, "v3")
        return auth0Users.map { userMapper.toUser(it) }
    }
}
