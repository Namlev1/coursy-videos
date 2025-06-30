package com.coursy.videos.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.Failure
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.ProcessingStatus
import com.coursy.videos.model.Thumbnail
import com.coursy.videos.model.ThumbnailType
import com.coursy.videos.repository.MetadataRepository
import com.coursy.videos.repository.ThumbnailRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class VideoProcessingService(
    private val fFmpegService: FFmpegService,
    private val fileManagementService: FileManagementService,
    private val hlsService: HlsService,
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
            val (hlsDir, thumbnailsDir) = fileManagementService.createTempDirs(tempDir)

            // Use HlsService instead of processing qualities and master playlist directly
            hlsService.processHls(hlsDir, originalVideo, metadata)

            val thumbnails = generateThumbnails(originalVideo, thumbnailsDir, metadata)
                .getOrElse {
                    logger.error("Failed to generate thumbnails for video $videoId: ${it.message()}")
                    setStatus(metadata, ProcessingStatus.FAILED)
                    return // todo handle
                }
            thumbnailRepository.saveAll(thumbnails)
            logger.info("Generated {} thumbnails for video {}", thumbnails.size, videoId)

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

    private fun generateThumbnails(
        inputFile: Path,
        outputDir: Path,
        metadata: Metadata
    ): Either<Failure, List<Thumbnail>> {
        val videoDuration = metadata.duration

        val thumbnails = mutableListOf<Thumbnail>()

        // Generate thumbnails at different timestamps (10%, 25%, 50% of video)
        val timestamps = listOf(
            videoDuration * 0.1,
            videoDuration * 0.25,
            videoDuration * 0.5
        )

        for (timestamp in timestamps) {
            for (size in ThumbnailType.entries) {
                val outputFile = outputDir.resolve("${timestamp.toInt()}_${size.name.lowercase()}.jpg")

                fFmpegService
                    .generateThumbnail(inputFile, outputFile, timestamp, size.width, size.height)
                    .onLeft { return it.left() }

                val objectPath = "${metadata.path}/thumbnails/${timestamp.toInt()}_${size.name.lowercase()}.jpg"

                fileManagementService.uploadThumbnail(outputFile, objectPath)

                thumbnails.add(
                    Thumbnail(
                        metadata = metadata,
                        path = outputFile.toString(),
                        timestampSeconds = timestamp,
                        thumbnailType = size,
                    )
                )
            }
        }

        return thumbnails.right()
    }

    private fun setStatus(metadata: Metadata, status: ProcessingStatus) {
        metadata.status = status
        metadataRepository.save(metadata)
    }
}
