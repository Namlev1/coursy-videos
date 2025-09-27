package com.coursy.videos.service

import arrow.core.getOrElse
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.ProcessingStatus
import com.coursy.videos.repository.MetadataRepository
import com.coursy.videos.repository.ThumbnailRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files

@Service
class VideoProcessingService(
    private val fFmpegService: FFmpegService,
    private val fileManagementService: FileManagementService,
    private val hlsService: HlsService,
    private val thumbnailsService: ThumbnailsService,
    private val metadataRepository: MetadataRepository,
    private val thumbnailRepository: ThumbnailRepository
) {
    private val logger = LoggerFactory.getLogger(VideoProcessingService::class.java)

    suspend fun processVideoAsync(metadata: Metadata, videoStream: InputStream) {
        val videoId = metadata.id
        logger.info("Started processing video $videoId")

        val tempDir = fileManagementService.createTempDir(videoId)

        setStatus(metadata, ProcessingStatus.PROCESSING)

        runCatching {
            val originalVideo = fileManagementService.downloadVideoFromMinio(tempDir, videoStream)
            logger.debug("Downloaded {} bytes for video {}", Files.size(originalVideo), videoId)

            val duration = fFmpegService.getVideoDuration(originalVideo).getOrElse {
                logger.error("Failed to get video duration for video $videoId: ${it.message()}")
                return //todo handle
            }
            logger.debug("Video duration detected: {}s for video {}", duration, videoId)
            metadata.duration = duration

            logger.debug("Creating temporary directories for video {}", videoId)
            val (hlsDir, thumbnailsDir) = fileManagementService.createHlsAndThumbnailDirs(tempDir)

            // Use HlsService instead of processing qualities and master playlist directly
            hlsService.processHls(hlsDir, originalVideo, metadata)

            val thumbnails = thumbnailsService.generateThumbnails(originalVideo, thumbnailsDir, metadata)
                .getOrElse {
                    logger.error("Failed to generate thumbnails for video $videoId: ${it.message()}")
                    setStatus(metadata, ProcessingStatus.FAILED)
                    return // todo handle
                }
            thumbnailRepository.saveAll(thumbnails)
            logger.info("Generated {} thumbnails for video {}", thumbnails.size, videoId)

            metadata.thumbnails.addAll(thumbnails)
            metadataRepository.save(metadata)
        }
            .fold(
                onSuccess = {
                    logger.info("Video processing completed successfully for video {}", videoId)
                    setStatus(metadata, ProcessingStatus.COMPLETED)
                },
                onFailure = { error ->
                    logger.error("Failed to process video {}", videoId, error)
                    setStatus(metadata, ProcessingStatus.FAILED)
                }
            )

        fileManagementService.cleanupDirectory(tempDir, videoId)

        logger.info("Finished processing video {}", videoId)
    }

    private fun setStatus(metadata: Metadata, status: ProcessingStatus) {
        metadata.status = status
        metadataRepository.save(metadata)
    }
}
