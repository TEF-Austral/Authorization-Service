package services

import dtos.CreateUserRequestDTO
import dtos.UserResponseDTO

class MockUserService : UserService {
    private val users = mutableMapOf<String, UserResponseDTO>()
    var createUserCalled = false
    var deleteUserCalled = false
    var lastCreatedUser: CreateUserRequestDTO? = null
    var lastDeletedUserId: String? = null
    var shouldThrowOnCreate = false
    var shouldThrowOnDelete = false

    override fun createUser(request: CreateUserRequestDTO): UserResponseDTO {
        createUserCalled = true
        lastCreatedUser = request

        if (shouldThrowOnCreate) {
            throw RuntimeException("Failed to create user")
        }

        val userId = "auth0|${request.email.hashCode()}"
        val response =
            UserResponseDTO(
                email = request.email,
                name = request.name,
                userId = userId,
            )
        users[userId] = response
        return response
    }

    override fun deleteUser(userId: String) {
        deleteUserCalled = true
        lastDeletedUserId = userId

        if (shouldThrowOnDelete) {
            throw RuntimeException("Failed to delete user")
        }

        if (!users.containsKey(userId)) {
            throw IllegalArgumentException("User not found")
        }

        users.remove(userId)
    }

    fun reset() {
        users.clear()
        createUserCalled = false
        deleteUserCalled = false
        lastCreatedUser = null
        lastDeletedUserId = null
        shouldThrowOnCreate = false
        shouldThrowOnDelete = false
    }

    fun addUser(user: UserResponseDTO) {
        users[user.userId] = user
    }

    fun getUser(userId: String): UserResponseDTO? = users[userId]
}
