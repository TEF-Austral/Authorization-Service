package api.controllers

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class ExampleController {

    @GetMapping("/")
    fun index(): Map<String, String> = mapOf("message" to "I'm Alive!")

    @GetMapping("/jwt")
    fun jwt(
        @AuthenticationPrincipal jwt: Jwt,
    ): String = jwt.tokenValue

    @GetMapping("/snippets")
    fun getAllSnippets(): Map<String, String> =
        mapOf("message" to "All snippets - read:snippets permission required")

    @GetMapping("/snippets/{id}")
    fun getSnippet(
        @PathVariable id: String,
    ): Map<String, String> =
        mapOf(
            "message" to "Snippet $id",
            "id" to id,
        )

    @PostMapping("/snippets")
    fun createSnippet(
        @RequestBody body: Map<String, Any>,
    ): Map<String, Any> =
        mapOf(
            "message" to "Snippet created - write:snippets permission required",
            "data" to body,
        )

    @GetMapping("/user-info")
    fun userInfo(
        @AuthenticationPrincipal jwt: Jwt,
    ): Map<String, Any?> =
        mapOf(
            "sub" to jwt.subject,
            "email" to jwt.claims["email"],
            "name" to jwt.claims["name"],
            "permissions" to jwt.claims["permissions"],
            "scopes" to jwt.claims["scope"],
        )
}
