package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.Failure
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.Thumbnail
import com.coursy.videos.model.ThumbnailType
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class ThumbnailsService(
    private val fFmpegService: FFmpegService,
    private val fileManagementService: FileManagementService
) {
    fun generateThumbnails(
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
}