package api.users.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class Auth0UserResponseDTO(
    @JsonProperty("user_id")
    val userId: String? = null,
    @JsonProperty("email")
    val email: String? = null,
    @JsonProperty("username")
    val username: String? = null,
    @JsonProperty("picture")
    val picture: String? = null,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("nickname")
    val nickname: String? = null,
)
