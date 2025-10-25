package api.dtos

data class GetSnippetPermissionsRequestDTO(
    val requesterId: String,
    val snippetId: String,
)
