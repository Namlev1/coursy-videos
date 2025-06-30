package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.Failure
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Service
class FileManagementService(
    private val minIoService: MinIOService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createTempDir(videoId: UUID): Path {
        val tempDir = Files.createTempDirectory("video_processing_$videoId")
        logger.debug("Created temp directory: {} for video {}", tempDir, videoId)
        return tempDir
    }

    fun createTempDirs(tempDir: Path): Pair<Path, Path> {
        val hlsDir = tempDir.resolve("hls")
        Files.createDirectories(hlsDir)
        val thumbnailsDir = tempDir.resolve("thumbnails")
        Files.createDirectories(thumbnailsDir)
        logger.debug("Created HLS directory: {} and thumbnails directory: {}", hlsDir, thumbnailsDir)
        return Pair(hlsDir, thumbnailsDir)
    }

    fun downloadVideoFromMinio(tempDir: Path, videoStream: InputStream): Path {
        val originalFile = tempDir.resolve("original.mp4")
        videoStream.use { stream ->
            Files.copy(stream, originalFile)
        }
        return originalFile
    }

    fun uploadHlsFiles(hlsDir: Path, qualityName: String, pathPrefix: String) {
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

    fun uploadMasterPlaylist(path: String, content: String) {
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

    fun uploadThumbnail(
        outputFile: Path,
        objectPath: String
    ): Either<Failure, Unit> {
        return Files
            .newInputStream(outputFile)
            .use { inputStream ->
                minIoService.uploadFile(
                    objectPath,
                    inputStream,
                    "image/jpeg",
                    Files.size(outputFile)
                ).fold(
                    { failure ->
                        logger.error("Failed to upload thumbnail: ${failure.message()}")
                        failure.left()
                    },
                    { Unit.right() }
                )
            }
    }

    fun cleanupDirectory(directory: Path, videoId: UUID) {
        runCatching {
            directory.toFile().deleteRecursively()
            logger.debug("Cleaned up temp directory for video {}", videoId)
        }.onFailure { exception ->
            logger.warn("Temporary directory cleanup failed for video {}: {}", videoId, exception.message)
        }
    }

    private fun getContentType(fileName: String): String {
        return when {
            fileName.endsWith(".ts") -> "video/mp2t"
            fileName.endsWith(".m3u8") -> "application/vnd.apple.mpegurl"
            else -> "application/octet-stream"
        }
    }
}