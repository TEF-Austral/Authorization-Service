package dtos

data class CheckPermissionRequestDTO(
    val userId: String,
    val action: String,
    val snippetId: String,
    val ownerId: String,
)
