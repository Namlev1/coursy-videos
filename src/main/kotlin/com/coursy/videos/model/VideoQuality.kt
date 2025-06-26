package com.coursy.videos.model

import com.coursy.videos.processing.SegmentInfo
import com.coursy.videos.processing.VideoQualityConfig
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate
import java.util.*

@Entity
class VideoQuality(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne
    val metadata: Metadata,

    val resolution: String,
    val bitrate: Int,
    val playlistPath: String,

    val segmentCount: Int,
    val avgSegmentDuration: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as VideoQuality

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    constructor(
        config: VideoQualityConfig,
        segmentInfo: SegmentInfo,
        metadata: Metadata
    ) : this(
        id = UUID.randomUUID(),
        metadata = metadata,
        resolution = config.resolution,
        bitrate = config.bitrate,
        playlistPath = "${metadata.path}/${config.name}",
        segmentCount = segmentInfo.segmentCount,
        avgSegmentDuration = segmentInfo.avgDuration
    )
} 
