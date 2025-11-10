package api.dtos.requests

data class GetSnippetPermissionsRequestDTO(
    val requesterId: String,
    val snippetId: String,
)
