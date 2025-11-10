package api.auth0

interface Auth0TokenManager {
    fun getManagementApiToken(): String
}
