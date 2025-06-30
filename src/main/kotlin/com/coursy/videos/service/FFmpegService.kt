package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.FFmpegFailure
import com.coursy.videos.processing.SegmentsInfo
import com.coursy.videos.processing.VideoQualityConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class FFmpegService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun processQuality(
        inputFile: Path,
        outputDir: Path,
        quality: VideoQualityConfig
    ): Either<FFmpegFailure, SegmentsInfo> {
        val command = listOf(
            "ffmpeg",
            "-i", inputFile.toString(),
            "-c:v", "libx264",
            "-c:a", "aac",
            "-s", quality.resolution,
            "-b:v", "${quality.bitrate}",
            "-maxrate", "${(quality.bitrate * 1.2).toInt()}",
            "-bufsize", "${quality.bitrate * 2}",
            "-hls_time", "6",                    // 6-seconds segments
            "-hls_playlist_type", "vod",
            "-hls_segment_filename", outputDir.resolve("segment_%03d.ts").toString(),
            outputDir.resolve("playlist.m3u8").toString()
        )
        logger.debug("Executing FFmpeg command for video quality {}: {}", quality.resolution, command.joinToString(" "))

        val process = ProcessBuilder(command).start()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            logger.error("FFmpeg failed with exit code {} for quality {}", exitCode, quality.resolution)
            return FFmpegFailure.ProcessingError(exitCode).left()
        }

        // Count segments
        val segmentFiles = Files.walk(outputDir)
            .filter { it.toString().endsWith(".ts") }
            .count()

        return SegmentsInfo(segmentFiles.toInt(), 6.0).right()
    }


    fun getVideoDuration(inputFile: Path): Either<FFmpegFailure, Double> {
        val command = listOf(
            "ffprobe",
            "-v", "quiet",
            "-show_entries", "format=duration",
            "-of", "csv=p=0",
            inputFile.toString()
        )
        logger.debug("Executing ffprobe command: {}", command.joinToString(" "))

        val process = ProcessBuilder(command).start()
        val output = process.inputStream.bufferedReader().readText().trim()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            logger.error("FFprobe failed with exit code {} for file {}", exitCode, inputFile)
            return FFmpegFailure.ProcessingError(exitCode).left()
        }

        return output.toDoubleOrNull()?.right()
            ?: FFmpegFailure.DurationParsingError.left()
    }

    fun generateThumbnail(
        inputFile: Path,
        outputFile: Path,
        timestamp: Double,
        width: Int,
        height: Int
    ): Either<FFmpegFailure.ProcessingError, Unit> {
        val command = listOf(
            "ffmpeg",
            "-i", inputFile.toString(),
            "-ss", timestamp.toString(),                    // Seek to timestamp
            "-vframes", "1",                               // Extract 1 frame
            "-vf", "scale=$width:$height",   // Resize
            "-q:v", "2",                                   // High quality (1-31, lower = better)
            "-y",                                          // Overwrite output files
            outputFile.toString()
        )

        val process = ProcessBuilder(command).start()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            return FFmpegFailure.ProcessingError(exitCode).left()
        }
        return Unit.right()
    }
}