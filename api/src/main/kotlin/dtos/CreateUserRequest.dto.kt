package dtos

data class CreateUserRequestDTO(
    val email: String,
    val password: String,
    val name: String,
)
