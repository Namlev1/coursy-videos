package com.coursy.clientvideoservice.model

import jakarta.persistence.*
import org.hibernate.Hibernate
import java.time.LocalDateTime

@Entity
data class Metadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val path: String, // MinIO object key

    @Column(nullable = false)
    val course: String,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val fileSize: Long,

    @Column(nullable = false)
    val uploadedAt: LocalDateTime = LocalDateTime.now(),

    // TODO implement duration with FFmpeg
    @Column
    val duration: Int? = null, // in seconds
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Metadata

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   title = $title )"
    }
}