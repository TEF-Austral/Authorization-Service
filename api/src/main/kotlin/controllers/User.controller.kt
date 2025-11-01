package controllers

import dtos.AddFriendRequestDTO
import dtos.AreFriendsRequestDTO
import dtos.AreFriendsResponseDTO
import dtos.FriendResponseDTO
import dtos.RemoveFriendRequestDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import services.FriendsService

@RestController
@RequestMapping("/api/users")
class UserController(
    private val friendsService: FriendsService,
) {

    @PostMapping("/friends")
    fun addFriend(
        @RequestBody request: AddFriendRequestDTO,
    ): ResponseEntity<FriendResponseDTO> {
        val friendship = friendsService.addFriend(request)
        return ResponseEntity.ok(friendship)
    }

    @PostMapping("/friends/remove")
    fun removeFriend(
        @RequestBody request: RemoveFriendRequestDTO,
    ): ResponseEntity<Void> {
        friendsService.removeFriend(request.userId1, request.userId2)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/friends/{userId1}/{userId2}")
    fun deleteFriend(
        @PathVariable userId1: String,
        @PathVariable userId2: String,
    ): ResponseEntity<Void> {
        friendsService.removeFriend(userId1, userId2)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/friends/check")
    fun areFriends(
        @RequestBody request: AreFriendsRequestDTO,
    ): ResponseEntity<AreFriendsResponseDTO> {
        val areFriends = friendsService.areFriends(request.userId1, request.userId2)
        return ResponseEntity.ok(AreFriendsResponseDTO(areFriends))
    }

    @GetMapping("/friends/{userId}")
    fun getFriendsByUser(
        @PathVariable userId: String,
    ): ResponseEntity<List<FriendResponseDTO>> {
        val friends = friendsService.getFriendsByUser(userId)
        return ResponseEntity.ok(friends)
    }

    @GetMapping("/friends")
    fun getAllFriendships(): ResponseEntity<List<FriendResponseDTO>> {
        val friendships = friendsService.getAllFriendships()
        return ResponseEntity.ok(friendships)
    }
}
