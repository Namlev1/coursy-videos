package com.coursy.videos.repository

import com.coursy.videos.model.Metadata
import com.coursy.videos.model.Thumbnail
import com.coursy.videos.model.ThumbnailSize
import com.coursy.videos.model.ThumbnailType
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.time.LocalDateTime
import java.util.*

interface ThumbnailRepository : JpaRepository<Thumbnail, UUID>, JpaSpecificationExecutor<Thumbnail>

class ThumbnailSpecification {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private val predicates = mutableListOf<Specification<Thumbnail>>()


        fun id(id: UUID?) = apply {
            id?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<UUID>("id"), it)
                }
            }
        }

        fun metadata(metadata: Metadata?) = apply {
            metadata?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<Metadata>("metadata"), it)
                }
            }
        }

        fun path(path: String?) = apply {
            path?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<String>("path"), it)
                }
            }
        }

        fun timestampSeconds(timestamp: Double?) = apply {
            timestamp?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<Double>("timestampSeconds"), it)
                }
            }
        }

        fun createdAt(createdAt: LocalDateTime?) = apply {
            createdAt?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<LocalDateTime>("createdAt"), it)
                }
            }
        }

        fun size(size: ThumbnailSize?) = apply {
            size?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<ThumbnailSize>("size"), it)
                }
            }
        }

        fun type(type: ThumbnailType?) = apply {
            type?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<ThumbnailType>("type"), it)
                }
            }
        }

        fun build(): Specification<Thumbnail> {
            return predicates.reduce { acc, spec -> acc.and(spec) }
        }
    }
}
