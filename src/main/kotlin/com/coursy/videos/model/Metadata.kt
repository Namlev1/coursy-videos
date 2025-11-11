package com.coursy.videos.model

import com.coursy.videos.types.FileName
import jakarta.persistence.*
import org.hibernate.Hibernate
import java.time.LocalDateTime
import java.util.*

@Entity
class Metadata(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val fileName: FileName,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    val path: String, // MinIO object key

    @Column(nullable = false)
    val course: UUID,

    @Column(nullable = false)
    val fileSize: Long,

    @Column(nullable = false)
    val uploadedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var status: ProcessingStatus,

    @Column
    var duration: Double = 0.0,

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @JoinColumn(name = "metadata_id")
    val thumbnails: MutableList<Thumbnail> = mutableListOf(),

    @Column
    override var position: Int
) : Ordered {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Metadata

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return "Metadata($fileName, $id)"
    }
}