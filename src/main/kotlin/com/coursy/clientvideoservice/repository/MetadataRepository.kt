package com.coursy.clientvideoservice.repository

import com.coursy.clientvideoservice.model.Metadata
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface MetadataRepository : JpaRepository<Metadata, Long>, JpaSpecificationExecutor<Metadata>

class VideoSpecification {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private val predicates = mutableListOf<Specification<Metadata>>()

        fun courseName(courseName: String?) = apply {
            courseName?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<String>("courseName"), it)
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

        fun build(): Specification<Metadata> {
            return predicates.reduce { acc, spec -> acc.and(spec) }
        }
    }
}