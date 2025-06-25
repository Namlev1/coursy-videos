package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.FFmpegFailure
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.ProcessingStatus
import com.coursy.videos.model.VideoQuality
import com.coursy.videos.processing.SegmentInfo
import com.coursy.videos.processing.VideoQualityConfig
import com.coursy.videos.repository.MetadataRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class VideoProcessingService(
    private val minIoService: MinIOService,
    private val metadataRepository: MetadataRepository,
) {
    private val logger = LoggerFactory.getLogger(VideoProcessingService::class.java)

    // todo: either async or suspend
    // todo finish this method, change to runCatching
    @Async
    fun processVideoAsync(metadata: Metadata, videoStream: InputStream) {
        logger.info("Started async processing")
        try {
            // Update status
            metadata.status = ProcessingStatus.PROCESSING
            metadataRepository.save(metadata) // todo important?

            // Download z MinIO do temp
            val tempDir = Files.createTempDirectory("video_processing_${metadata.id}")
            val originalFile = tempDir.resolve("original.mp4")

            videoStream.use { stream ->
                Files.copy(stream, originalFile)
            }

            val qualities = listOf(
                VideoQualityConfig("480p", "854x480", 800000),
                VideoQualityConfig("720p", "1280x720", 1400000),
                VideoQualityConfig("1080p", "1920x1080", 2800000)
            )

            val hlsDir = tempDir.resolve("hls")
            Files.createDirectories(hlsDir)

            mutableListOf<VideoQuality>()
            val masterPlaylistContent = StringBuilder()
            masterPlaylistContent.appendLine("#EXTM3U")
            masterPlaylistContent.appendLine("#EXT-X-INDEPENDENT-SEGMENTS")

            // Przetwarzaj każdą jakość
            for (quality in qualities) {
                val qualityDir = hlsDir.resolve(quality.name)
                Files.createDirectories(qualityDir)

                // FFmpeg command
                processQuality(originalFile, qualityDir, quality)
                    .onLeft {
                        logger.error(it.message())
                        return
                    } // todo handle

                // Upload segmentów do MinIO
                // Zapisz jakość do DB

                masterPlaylistContent.appendLine("#EXT-X-STREAM-INF:BANDWIDTH=${quality.bitrate},RESOLUTION=${quality.resolution}")
                masterPlaylistContent.appendLine("${quality.name}/playlist.m3u8")
            }

            // Upload master playlist to minIo
            // Save qualities

            metadata.status = ProcessingStatus.COMPLETED
            metadataRepository.save(metadata)

            // Cleanup
            tempDir.toFile().deleteRecursively()

        } catch (e: Exception) {
            logger.error("Failed to process video ${metadata.id}", e)
            metadata.status = ProcessingStatus.FAILED
            metadataRepository.save(metadata)
        }
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

        // Policz segmenty
        val segmentFiles = Files.walk(outputDir)
            .filter { it.toString().endsWith(".ts") }
            .count()

        return SegmentInfo(segmentFiles.toInt(), 6.0).right()
    }
}