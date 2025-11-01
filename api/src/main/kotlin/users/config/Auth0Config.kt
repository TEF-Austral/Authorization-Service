package api.users.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth0")
data class Auth0Config(
    var audience: String = "",
    var domain: String = "",
    var management: Management = Management(),
) {
    data class Management(
        var clientId: String = "",
        var clientSecret: String = "",
    )
}
