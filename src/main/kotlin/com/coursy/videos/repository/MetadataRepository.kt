package com.coursy.videos.repository

import com.coursy.videos.model.Metadata
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface MetadataRepository : JpaRepository<Metadata, UUID>, JpaSpecificationExecutor<Metadata>

class MetadataSpecification {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private val predicates = mutableListOf<Specification<Metadata>>()

        fun courseName(course: String?) = apply {
            course?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<String>("course"), it)
                }
            }
        }

        fun userId(userId: Long?) = apply {
            userId?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<Long>("userId"), it)
                }
            }
        }

        fun fileName(fileName: String?) = apply {
            fileName?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<Long>("title"), it)
                }
            }
        }

        fun build(): Specification<Metadata> {
            return predicates.reduce { acc, spec -> acc.and(spec) }
        }
    }
}