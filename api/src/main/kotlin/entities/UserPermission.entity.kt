package api.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "user_permissions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_snippet_permission",
            columnNames = ["user_id", "snippet_id", "permission"],
        ),
    ],
    indexes = [
        Index(name = "idx_perm_user", columnList = "user_id"),
        Index(name = "idx_perm_snippet", columnList = "snippet_id"),
    ],
)
data class UserPermission(
    @Id @GeneratedValue
    val id: Long = 0,
    @Column(nullable = false, name = "user_id")
    val userId: String,
    @Column(nullable = false, name = "snippet_id")
    val snippetId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val permission: PermissionType,
)
