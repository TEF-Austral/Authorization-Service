package auth

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties =
        ["spring.security.oauth2.resourceserver.jwt.issuer-uri=https://tef-austral.us.auth0.com/"],
)
class AuthenticationServiceApplicationTests {

    //    @Test
    //    fun contextLoads() {
    //    }
}
