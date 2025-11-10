package api.dtos.requests

data class GrantPermissionRequestDTO(
    val requesterId: String,
    val ownerId: String,
    val granteeId: String,
    val snippetId: String,
    val canRead: Boolean,
    val canEdit: Boolean,
)
