package api.dtos

import api.entities.PermissionType

data class ShareRequestDTO(
    val userIdToShareWith: String,
    val permissionToGrant: PermissionType,
)
