package com.coursy.videos.service

import arrow.core.getOrElse
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.VideoQuality
import com.coursy.videos.processing.VideoQualityConfig
import com.coursy.videos.repository.VideoQualityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class HlsService(
    private val fFmpegService: FFmpegService,
    private val fileManagementService: FileManagementService,
    private val videoQualityRepository: VideoQualityRepository
) {
    private val logger = LoggerFactory.getLogger(HlsService::class.java)

    private val qualities = listOf(
        VideoQualityConfig("480p", "854x480", 800000),
        VideoQualityConfig("720p", "1280x720", 1400000),
        VideoQualityConfig("1080p", "1920x1080", 2800000)
    )

    fun processHls(hlsDir: Path, originalVideo: Path, metadata: Metadata) {
        processQualities(hlsDir, originalVideo, metadata)
        processMasterPlaylist(metadata)
    }

    // todo parallel after you're done with entire method
    private fun processQualities(
        hlsDir: Path,
        originalVideo: Path,
        metadata: Metadata
    ) {
        val videoId = metadata.id
        logger.info(
            "Starting quality processing for video {} (qualities: {})",
            videoId,
            qualities.map { it.resolution })

        for (quality in qualities) {
            logger.info("Started processing quality {} for video {}", quality.resolution, videoId)

            val qualityDir = fileManagementService.createQualityDir(hlsDir, quality, videoId)

            val segmentsInfo = fFmpegService.processQuality(originalVideo, qualityDir, quality)
                .getOrElse {
                    logger.error(
                        "FFmpeg processing failed for video {} quality {}: {}",
                        videoId,
                        quality.resolution,
                        it.message()
                    )
                    return // todo handle
                } 

            logger.debug(
                "Generated {} HLS segments for video {} quality {}",
                segmentsInfo.segmentsCount,
                videoId,
                quality.resolution
            )
            fileManagementService.uploadHlsQualityDir(qualityDir, quality.name, metadata.path)

            val qualityToPersist = VideoQuality(
                quality,
                segmentsInfo,
                metadata
            )
            videoQualityRepository.save(qualityToPersist)

            logger.info("Completed quality {} for video {}", quality.resolution, videoId)
        }
        logger.info("Finished processing all qualities for video {}", videoId)
    }

    private fun processMasterPlaylist(metadata: Metadata) {
        val videoId = metadata.id
        logger.debug("Creating master playlist for video {}", videoId)

        val masterPlaylistContent = StringBuilder().apply {
            appendLine("#EXTM3U")
            appendLine("#EXT-X-VERSION:3")
            appendLine("#EXT-X-INDEPENDENT-SEGMENTS")

            qualities.forEach { quality ->
                appendLine("#EXT-X-STREAM-INF:BANDWIDTH=${quality.bitrate},RESOLUTION=${quality.resolution}")
                appendLine("${quality.name}/playlist.m3u8")
            }
        }

        logger.debug("Master playlist content for video {}: {}", videoId, masterPlaylistContent.toString())
        fileManagementService.uploadMasterPlaylist(metadata.path, masterPlaylistContent.toString())
        logger.debug("Uploaded master playlist for video {}", videoId)
    }
}