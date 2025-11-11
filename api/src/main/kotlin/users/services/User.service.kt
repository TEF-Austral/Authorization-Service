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
    private val log = org.slf4j.LoggerFactory.getLogger(UserService::class.java)

    fun getUsers(
        query: String? = null,
        page: Int = 0,
        pageSize: Int = 50,
    ): PaginatedUsersDTO {
        log.info("Fetching users with query: $query, page: $page, pageSize: $pageSize")
        val auth0Users = auth0UserRepository.getUsers(query, page, pageSize, "v3")
        val users = auth0Users.map { userMapper.toUser(it) }

        val result =
            PaginatedUsersDTO(
                users = users,
                page = page,
                pageSize = pageSize,
                total = users.size,
            )
        log.warn("Retrieved ${users.size} users")
        return result
    }

    fun getUserById(userId: String): User {
        log.info("Fetching user by ID: $userId")
        val auth0User = auth0UserRepository.getUserById(userId)
        val user = userMapper.toUser(auth0User)
        log.warn("User $userId retrieved successfully")
        return user
    }

    fun getUsersByEmail(email: String): List<User> {
        log.info("Fetching users by email: $email")
        val auth0Users = auth0UserRepository.getUsersByEmail(email)
        val users = auth0Users.map { userMapper.toUser(it) }
        log.warn("Retrieved ${users.size} users with email: $email")
        return users
    }

    fun searchUsers(
        name: String? = null,
        email: String? = null,
        emailVerified: Boolean? = null,
        connection: String? = null,
    ): List<User> {
        log.info(
            "Searching users with name: $name, email: $email, emailVerified: $emailVerified, connection: $connection",
        )
        val query = queryBuilder.buildSearchQuery(name, email, emailVerified, connection)
        val auth0Users = auth0UserRepository.getUsers(query, 0, 50, "v3")
        val users = auth0Users.map { userMapper.toUser(it) }
        log.warn("Found ${users.size} users matching search criteria")
        return users
    }
}
