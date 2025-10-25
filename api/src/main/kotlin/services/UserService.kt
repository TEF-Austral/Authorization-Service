package services

import dtos.CreateUserRequestDTO
import dtos.UserResponseDTO

interface UserService {
    fun createUser(request: CreateUserRequestDTO): UserResponseDTO

    fun deleteUser(userId: String)
}
