package com.coursy.videos.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.FFmpegFailure
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.ProcessingStatus
import com.coursy.videos.model.VideoQuality
import com.coursy.videos.processing.SegmentInfo
import com.coursy.videos.processing.VideoQualityConfig
import com.coursy.videos.repository.MetadataRepository
import com.coursy.videos.repository.VideoQualityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class VideoProcessingService(
    private val minIoService: MinIOService,
    private val metadataRepository: MetadataRepository,
    private val videoQualityRepository: VideoQualityRepository
) {
    private val logger = LoggerFactory.getLogger(VideoProcessingService::class.java)
    private val qualities = listOf(
        VideoQualityConfig("480p", "854x480", 800000),
        VideoQualityConfig("720p", "1280x720", 1400000),
        VideoQualityConfig("1080p", "1920x1080", 2800000)
    )

    // todo finish this method
    suspend fun processVideoAsync(metadata: Metadata, videoStream: InputStream) {
        logger.info("Started async processing")

        val tempDir = Files.createTempDirectory("video_processing_${metadata.id}")
        runCatching {
            // Update status
            metadata.status = ProcessingStatus.PROCESSING
            metadataRepository.save(metadata)

            // Download z MinIO do temp
            val originalFile = tempDir.resolve("original.mp4")
            videoStream.use { stream ->
                Files.copy(stream, originalFile)
            }

            val hlsDir = tempDir.resolve("hls")
            Files.createDirectories(hlsDir)

            // todo parallel after you're done with entire method
            for (quality in qualities) {
                logger.info("Started processing quality ${quality.resolution} for video: ${metadata.id}")

                val qualityDir = hlsDir.resolve(quality.name)
                Files.createDirectories(qualityDir)

                // FFmpeg command
                val segmentInfo = processQuality(originalFile, qualityDir, quality)
                    .getOrElse {
                        logger.error(it.message())
                        return
                    } // todo handle

                uploadHlsSegments(qualityDir, quality.name, metadata.path)

                val qualityToPersist = VideoQuality(
                    quality,
                    segmentInfo,
                    metadata
                )
                videoQualityRepository.save(qualityToPersist)

                logger.info("Finished processing quality ${quality.resolution} for video: ${metadata.id}")
            }

            val masterPlaylistContent = StringBuilder().apply {
                appendLine("#EXTM3U")
                appendLine("#EXT-X-VERSION:3")
                appendLine("#EXT-X-INDEPENDENT-SEGMENTS")

                // Add each quality
                qualities.forEach { quality ->
                    appendLine("#EXT-X-STREAM-INF:BANDWIDTH=${quality.bitrate},RESOLUTION=${quality.resolution}")
                    appendLine("${quality.name}/playlist.m3u8")
                }
            }
            uploadMasterPlaylist(metadata.path, masterPlaylistContent.toString())

            metadata.status = ProcessingStatus.COMPLETED
            metadataRepository.save(metadata)
        }
            .fold(
                onSuccess = {
                    logger.info("Video processing completed successfully for ${metadata.id}")

                    metadata.status = ProcessingStatus.COMPLETED
                    metadataRepository.save(metadata)
                },
                onFailure = { error ->
                    logger.error("Failed to process video ${metadata.id}", error)
                    metadata.status = ProcessingStatus.FAILED
                    metadataRepository.save(metadata)
                }
            )

        tempDir.toFile().deleteRecursively()
        logger.info("Finished async processing")
    }

    private fun processQuality(
        inputFile: Path,
        outputDir: Path,
        quality: VideoQualityConfig
    ): Either<FFmpegFailure, SegmentInfo> {
        val command = listOf(
            "ffmpeg",
            "-i", inputFile.toString(),
            "-c:v", "libx264",
            "-c:a", "aac",
            "-s", quality.resolution,
            "-b:v", "${quality.bitrate}",
            "-maxrate", "${(quality.bitrate * 1.2).toInt()}",
            "-bufsize", "${quality.bitrate * 2}",
            "-hls_time", "6",                    // 6-sekundowe segmenty
            "-hls_playlist_type", "vod",
            "-hls_segment_filename", outputDir.resolve("segment_%03d.ts").toString(),
            outputDir.resolve("playlist.m3u8").toString()
        )

        val process = ProcessBuilder(command).start()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            return FFmpegFailure(exitCode).left()
        }

        // Count segments
        val segmentFiles = Files.walk(outputDir)
            .filter { it.toString().endsWith(".ts") }
            .count()

        return SegmentInfo(segmentFiles.toInt(), 6.0).right()
    }

    private fun uploadHlsSegments(hlsDir: Path, qualityName: String, pathPrefix: String) {
        Files.walk(hlsDir).forEach { file ->
            if (Files.isRegularFile(file)) {
                val fileName = file.fileName.toString()
                val objectPath = "$pathPrefix/$qualityName/$fileName"
                val inputStream = Files.newInputStream(file)

                minIoService
                    .uploadFile(
                        objectPath,
                        inputStream,
                        getContentType(fileName),
                        Files.size(file)
                    )
                    .onLeft { logger.error(it.message()) }
            }
        }
    }

    private fun uploadMasterPlaylist(path: String, content: String) {
        val contentBytes = content.toByteArray()

        minIoService
            .uploadFile(
                "$path/master.m3u8",
                contentBytes.inputStream(),
                "application/vnd.apple.mpegurl",
                contentBytes.size.toLong()
            )
            .onLeft { logger.error(it.message()) }
    }

    private fun getContentType(fileName: String): String {
        return when {
            fileName.endsWith(".ts") -> "video/mp2t"
            fileName.endsWith(".m3u8") -> "application/vnd.apple.mpegurl"
            else -> "application/octet-stream"
        }
    }
}
