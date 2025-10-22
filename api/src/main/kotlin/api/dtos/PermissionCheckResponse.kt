package api.dtos

data class PermissionCheckResponse(
    val hasPermission: Boolean,
    val reason: String? = null
)
