package com.coursy.videos.model

import jakarta.persistence.*
import org.hibernate.Hibernate
import java.util.*

@Entity
class VideoQuality(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne
    val metadata: Metadata,

    val resolution: String, // "1920x1080", "1280x720"
    val bitrate: Int,       // 2800000
    val playlistPath: String, // "videos/tenant1/hls/1080p/playlist.m3u8"

    val segmentCount: Int,
    val avgSegmentDuration: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as VideoQuality

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

} 
