package api.users.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class Auth0UserResponseDTO(
    @JsonProperty("user_id")
    val userId: String? = null,
    @JsonProperty("email")
    val email: String? = null,
    @JsonProperty("email_verified")
    val emailVerified: Boolean? = null,
    @JsonProperty("username")
    val username: String? = null,
    @JsonProperty("phone_number")
    val phoneNumber: String? = null,
    @JsonProperty("phone_verified")
    val phoneVerified: Boolean? = null,
    @JsonProperty("created_at")
    val createdAt: String? = null,
    @JsonProperty("updated_at")
    val updatedAt: String? = null,
    @JsonProperty("identities")
    val identities: List<Identity>? = null,
    @JsonProperty("app_metadata")
    val appMetadata: Map<String, Any>? = null,
    @JsonProperty("user_metadata")
    val userMetadata: Map<String, Any>? = null,
    @JsonProperty("picture")
    val picture: String? = null,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("nickname")
    val nickname: String? = null,
    @JsonProperty("multifactor")
    val multifactor: List<String>? = null,
    @JsonProperty("last_ip")
    val lastIp: String? = null,
    @JsonProperty("last_login")
    val lastLogin: String? = null,
    @JsonProperty("logins_count")
    val loginsCount: Int? = null,
    @JsonProperty("blocked")
    val blocked: Boolean? = null,
    @JsonProperty("given_name")
    val givenName: String? = null,
    @JsonProperty("family_name")
    val familyName: String? = null,
) {
    data class Identity(
        @JsonProperty("connection")
        val connection: String? = null,
        @JsonProperty("user_id")
        val userId: String? = null,
        @JsonProperty("provider")
        val provider: String? = null,
        @JsonProperty("isSocial")
        val isSocial: Boolean? = null,
    )
}
