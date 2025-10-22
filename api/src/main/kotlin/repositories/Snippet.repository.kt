package api.repositories

import api.entities.Snippet
import org.springframework.data.jpa.repository.JpaRepository

interface SnippetRepository : JpaRepository<Snippet, Long> {

    fun findByIdAndOwnerId(
        id: Long,
        ownerId: String,
    ): Snippet?
}
