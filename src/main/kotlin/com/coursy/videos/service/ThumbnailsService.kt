package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.Failure
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.Thumbnail
import com.coursy.videos.model.ThumbnailSize
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


        val timestamps = listOf(
            Pair(videoDuration * 0.1, ThumbnailType.TEN),
            Pair(videoDuration * 0.25, ThumbnailType.TWENTY_FIVE),
            Pair(videoDuration * 0.5, ThumbnailType.FIFTY)
        )

        for ((timestamp, type) in timestamps) {
            for (size in ThumbnailSize.entries) {
                val outputFile = outputDir.resolve("${timestamp.toInt()}_${size.name.lowercase()}.jpg")

                fFmpegService
                    .generateThumbnail(inputFile, outputFile, timestamp, size.width, size.height)
                    .onLeft { return it.left() }

                val objectPath = "${metadata.path}/thumbnails/${timestamp.toInt()}_${size.name.lowercase()}.jpg"

                fileManagementService.uploadThumbnail(outputFile, objectPath)
                val isPrimary = type == ThumbnailType.TEN

                val thumbnail = Thumbnail(
                    path = objectPath,
                    timestampSeconds = timestamp,
                    size = size,
                    type = type,
                    primary = isPrimary
                )

                thumbnails.add(thumbnail)
            }
        }

        return thumbnails.right()
    }
}