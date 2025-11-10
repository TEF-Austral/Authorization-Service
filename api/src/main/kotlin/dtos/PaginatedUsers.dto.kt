package api.dtos

import api.users.models.User

data class PaginatedUsersDTO(
    val users: List<User>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
)
