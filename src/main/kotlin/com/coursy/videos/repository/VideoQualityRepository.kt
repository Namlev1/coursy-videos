package com.coursy.videos.repository

import com.coursy.videos.model.VideoQuality
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface VideoQualityRepository : JpaRepository<VideoQuality, UUID> {
    fun deleteByMetadataId(metadataId: UUID)
}
