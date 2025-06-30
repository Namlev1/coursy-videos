package com.coursy.videos.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.Failure
import com.coursy.videos.model.*
import com.coursy.videos.processing.VideoQualityConfig
import com.coursy.videos.repository.MetadataRepository
import com.coursy.videos.repository.ThumbnailRepository
import com.coursy.videos.repository.VideoQualityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

// TODO extract HlsService
@Service
class VideoProcessingService(
    private val fFmpegService: FFmpegService,
    private val fileManagementService: FileManagementService,
    private val metadataRepository: MetadataRepository,
    private val videoQualityRepository: VideoQualityRepository,
    private val thumbnailRepository: ThumbnailRepository
) {
    private val logger = LoggerFactory.getLogger(VideoProcessingService::class.java)
    private val qualities = listOf(
        VideoQualityConfig("480p", "854x480", 800000),
        VideoQualityConfig("720p", "1280x720", 1400000),
        VideoQualityConfig("1080p", "1920x1080", 2800000)
    )

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

            processQualities(hlsDir, originalVideo, metadata)
            processMasterPlaylist(metadata)

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

            val qualityDir = hlsDir.resolve(quality.name)
            Files.createDirectories(qualityDir)
            logger.debug("Created quality directory: {} for video {}", qualityDir, videoId)

            // FFmpeg command
            val segmentsInfo = fFmpegService.processQuality(originalVideo, qualityDir, quality)
                .getOrElse {
                    logger.error(
                        "FFmpeg processing failed for video {} quality {}: {}",
                        videoId,
                        quality.resolution,
                        it.message()
                    )
                    return
                } // todo handle

            logger.debug(
                "Generated {} HLS segments for video {} quality {}",
                segmentsInfo.segmentsCount,
                videoId,
                quality.resolution
            )
            fileManagementService.uploadHlsFiles(qualityDir, quality.name, metadata.path)

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
