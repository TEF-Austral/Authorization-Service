package entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val userId: String,
    @Column(nullable = false)
    val snippetId: String,
    @Column(nullable = false)
    val canRead: Boolean = false,
    @Column(nullable = false)
    val canEdit: Boolean = false,
)
