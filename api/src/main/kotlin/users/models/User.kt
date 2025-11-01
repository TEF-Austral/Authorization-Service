package api.users.models

data class User(
    val id: String,
    val username: String?,
    val email: String?,
    val name: String?,
    val picture: String?,
    val emailVerified: Boolean?,
    val createdAt: String?,
    val lastLogin: String?,
)
