package api.entities

import jakarta.persistence.*

@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val resource: String,
    val action: String,

    @Column(unique = true)
    val name: String
)
