package api.dtos.requests

data class RevokePermissionRequestDTO(
    val requesterId: String,
    val userId: String,
    val snippetId: String,
)
