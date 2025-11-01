package services

import dtos.AddFriendRequestDTO
import dtos.FriendResponseDTO
import entities.Friends
import org.springframework.stereotype.Service
import repositories.FriendsRepository

@Service
class FriendsService(
    private val friendsRepository: FriendsRepository,
) {

    fun addFriend(request: AddFriendRequestDTO): FriendResponseDTO {
        // Verificar si ya existe la relación
        val existingFriendship =
            friendsRepository.findByUserId1AndUserId2(
                request.userId1,
                request.userId2,
            )

        if (existingFriendship != null) {
            return toDTO(existingFriendship)
        }

        // Crear nueva relación de amistad
        val friendship =
            Friends(
                userId1 = request.userId1,
                userId2 = request.userId2,
            )

        val savedFriendship = friendsRepository.save(friendship)
        return toDTO(savedFriendship)
    }

    fun removeFriend(
        userId1: String,
        userId2: String,
    ) {
        friendsRepository.deleteByUserId1AndUserId2(userId1, userId2)
    }

    fun areFriends(
        userId1: String,
        userId2: String,
    ): Boolean {
        // Verificar en ambas direcciones
        return friendsRepository.existsByUserId1AndUserId2(userId1, userId2) ||
            friendsRepository.existsByUserId1AndUserId2(userId2, userId1)
    }

    fun getFriendsByUser(userId: String): List<FriendResponseDTO> {
        // Obtener amigos donde el usuario es userId1
        val friendsAsUser1 = friendsRepository.findAllByUserId1(userId)
        // Obtener amigos donde el usuario es userId2
        val friendsAsUser2 = friendsRepository.findAllByUserId2(userId)

        return (friendsAsUser1 + friendsAsUser2).map { toDTO(it) }
    }

    fun getAllFriendships(): List<FriendResponseDTO> = friendsRepository.findAll().map { toDTO(it) }

    private fun toDTO(friends: Friends): FriendResponseDTO =
        FriendResponseDTO(
            id = friends.id,
            userId1 = friends.userId1,
            userId2 = friends.userId2,
        )
}
