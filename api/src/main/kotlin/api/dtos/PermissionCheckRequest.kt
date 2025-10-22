package api.dtos

data class PermissionCheckRequest(
    val userId: String,
    val resource: String,
    val action: String,
    val resourceId: String? = null
)
