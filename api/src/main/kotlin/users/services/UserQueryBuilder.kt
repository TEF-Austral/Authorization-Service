package api.users.services

import org.springframework.stereotype.Component

@Component
class UserQueryBuilder {

    fun buildSearchQuery(
        name: String?,
        email: String?,
        emailVerified: Boolean?,
        connection: String?,
    ): String? {
        val queryParts = mutableListOf<String>()

        name?.let { queryParts.add("name:*$it*") }
        email?.let { queryParts.add("email:\"$it\"") }
        emailVerified?.let { queryParts.add("email_verified:$it") }
        connection?.let { queryParts.add("identities.connection:\"$it\"") }

        return if (queryParts.isNotEmpty()) {
            queryParts.joinToString(" AND ")
        } else {
            null
        }
    }
}
