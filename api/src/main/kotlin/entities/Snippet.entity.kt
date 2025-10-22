package api.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "snippets",
    indexes = [
        // Agregamos un índice a 'owner_id' porque es probable
        // que quieras buscar "todos los snippets de un usuario".
        Index(name = "idx_snippet_owner", columnList = "owner_id"),
    ],
)
data class Snippet(
    @Id @GeneratedValue
    val id: Long,
    // Usamos 'TEXT' para contenido largo, no el VARCHAR(255) por defecto
    @Column(nullable = false)
    val snippetInBucket: Long,
    // Mapeamos 'ownerId' a 'owner_id' y nos aseguramos de que no sea nulo
    @Column(nullable = false, name = "owner_id")
    val ownerId: String,
)
