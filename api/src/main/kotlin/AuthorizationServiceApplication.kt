package api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
@ComponentScan(
    basePackages = [
        "api", "controllers", "services",
        "security", "dtos", "repositories",
        "entities", "auth0", "config", "users",
    ],
)
@EnableJpaRepositories(basePackages = ["repositories"])
@EntityScan(basePackages = ["entities"])
class AuthorizationServiceApplication {

    @GetMapping("/")
    fun index(): String = "Authorization Service v1.0"

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "UP")
}

fun main(args: Array<String>) {
    runApplication<AuthorizationServiceApplication>(*args)
}
