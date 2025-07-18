package com.coursy.videos.model

import jakarta.persistence.*
import org.hibernate.Hibernate
import java.time.LocalDateTime
import java.util.*

@Entity
class Thumbnail(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne
    val metadata: Metadata,
    val path: String,
    val timestampSeconds: Double,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Enumerated(EnumType.STRING)
    val size: ThumbnailSize,
    @Enumerated(EnumType.STRING)
    val type: ThumbnailType,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Thumbnail

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}

enum class ThumbnailSize(val width: Int, val height: Int) {
    SMALL(150, 84),
    MEDIUM(320, 180),
    LARGE(640, 360),
}

enum class ThumbnailType {
    /** 10% of the video */
    TEN,

    /** 25% of the video */
    TWENTY_FIVE,

    /** 50% of the video */
    FIFTY,

    /** Custom uploaded thumbnail */
    CUSTOM
}