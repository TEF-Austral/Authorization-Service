package api.users.services

import api.dtos.responses.Auth0UserResponseDTO
import api.users.models.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toUser(auth0User: Auth0UserResponseDTO): User =
        User(
            id = auth0User.userId ?: "",
            username = auth0User.username ?: auth0User.nickname,
            email = auth0User.email,
            name = auth0User.name,
            picture = auth0User.picture,
        )
}
