package com.coursy.videos.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.videos.dto.MetadataResponse
import com.coursy.videos.dto.StreamData
import com.coursy.videos.dto.StreamingResult
import com.coursy.videos.dto.toResponse
import com.coursy.videos.failure.*
import com.coursy.videos.model.Metadata
import com.coursy.videos.model.ProcessingStatus
import com.coursy.videos.model.ThumbnailSize
import com.coursy.videos.model.ThumbnailType
import com.coursy.videos.repository.MetadataRepository
import com.coursy.videos.repository.MetadataSpecification
import com.coursy.videos.repository.ThumbnailRepository
import com.coursy.videos.repository.ThumbnailSpecification
import com.coursy.videos.types.ContentType
import com.coursy.videos.types.FileName
import com.coursy.videos.utils.toEither
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
@Transactional
class VideoService(
    private val minioService: MinIOService,
    private val metadataRepository: MetadataRepository,
    private val thumbnailRepository: ThumbnailRepository,
    private val pagedResourcesAssembler: PagedResourcesAssembler<MetadataResponse>,
    private val videoProcessingService: VideoProcessingService
) {
    private val processingScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    fun saveVideo(
        file: MultipartFile,
        userId: Long,
        course: String,
    ): Either<Failure, MetadataResponse> {
        if (file.isEmpty) {
            return FileFailure.Empty.left()
        }
        val fileName = FileName.fromFile(file).getOrElse { return it.left() }
        val contentType = ContentType.fromFile(file).getOrElse { return it.left() }
        val dir = "$userId/$course/${fileName.withoutExt()}"

        if (fileAlreadyExists(fileName, userId, course)) {
            return FileFailure.AlreadyExists.left()
        }

        // Save metadata first
        var metadata = Metadata(
            title = fileName,
            path = dir,
            course = course,
            userId = userId,
            fileSize = file.size,
            status = ProcessingStatus.UPLOADED
        )
        metadata = metadataRepository.save(metadata)

        // Save file in MinIO
        minioService.uploadFile(
            path = "$dir/${fileName.value}",
            inputStream = file.inputStream,
            contentType = contentType.value,
            size = file.size
        ).onLeft { return it.left() }

        metadataRepository.flush()
        processingScope.launch {
            videoProcessingService.processVideoAsync(metadata, file.inputStream)
        }

        return metadata.toResponse().right()
    }

    fun getVideoStream(
        videoId: UUID,
        userId: Long,
        course: String,
    ): Either<Failure, StreamData> {
        // TODO authorization: users can only download its videos, ADMIN can download any.
        val metadata = metadataRepository.findById(videoId).getOrElse { return FileFailure.InvalidId.left() }
        val fileSize = metadata.fileSize
        val fileName = metadata.title

        val path = "$userId/$course/${fileName.value}"


        return minioService
            .getFileStream(path)
            .map { inputStream ->
                val streamingBody = StreamingResponseBody { outputStream ->
                    inputStream.use { input ->
                        input.copyTo(outputStream)
                    }
                }

                StreamData(
                    streamingBody,
                    fileSize,
                    fileName
                )
            }
    }

    fun streamVideo(
        fileId: UUID,
        rangeHeader: String,
    ): Either<Failure, StreamingResult> {
        val metadata = metadataRepository.findById(fileId).getOrElse { return FileFailure.InvalidId.left() }
        val fileSize = metadata.fileSize
        val (start, end) = parseRange(rangeHeader, fileSize)
        if (start >= metadata.fileSize - 1)
            return RangeFailure(start, end).left()
        val actualEnd = minOf(end, fileSize)
        val path = "${metadata.userId}/${metadata.course}/${metadata.title}"

        return minioService
            .getFileStream(path)
            .map { inputStream ->
                // Pomiń pierwsze 'start' bajtów
                var skipped = 0L
                while (skipped < start) {
                    val toSkip = minOf(8192, start - skipped) // Skip w blokach 8KB
                    val actuallySkipped = inputStream.skip(toSkip)
                    if (actuallySkipped == 0L) break // EOF reached
                    skipped += actuallySkipped
                }

                // Skopiuj tylko requested range
                val bytesToCopy = actualEnd - start + 1

                val streamingBody = StreamingResponseBody { outputStream ->
                    inputStream.use { input ->
                        copyLimitedBytes(inputStream, outputStream, bytesToCopy)
                    }
                }

                StreamingResult(
                    streamingBody,
                    start,
                    actualEnd,
                    fileSize
                )
            }

    }

    fun getVideo(videoId: UUID): Either<FileFailure, MetadataResponse> =
        metadataRepository.findById(videoId)
            .toEither(
                { FileFailure.InvalidId.left() },
                { it.toResponse().right() }
            )

    fun getPage(pageRequest: PageRequest) =
        metadataRepository.findAll(pageRequest)
            .map { it.toResponse() }
            .let { pagedResourcesAssembler.toModel(it) }

    // todo don't download if processing is not finished
    fun getMasterPlaylist(videoId: UUID): Either<Failure, String> {
        val metadata = metadataRepository
            .findById(videoId)
            .getOrElse { return MetadataFailure.NotFound.left() }
        val path = metadata.path + "/master.m3u8"

        return minioService
            .getFileStream(path)
            .map { inputStream ->
                inputStream.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }
            }
    }

    fun getQualityPlaylist(videoId: UUID, quality: String): Either<Failure, String> {
        val metadata = metadataRepository
            .findById(videoId)
            .getOrElse { return MetadataFailure.NotFound.left() }
        val path = "${metadata.path}/$quality/playlist.m3u8"

        return minioService
            .getFileStream(path)
            .map { inputStream ->
                inputStream.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }
            }
    }

    fun getSegment(
        videoId: UUID,
        quality: String,
        segmentName: String,
    ): Either<Failure, StreamingResponseBody> {
        val metadata = metadataRepository
            .findById(videoId)
            .getOrElse { return MetadataFailure.NotFound.left() }
        val path = "${metadata.path}/$quality/$segmentName"

        return minioService
            .getFileStream(path)
            .map { inputStream ->
                StreamingResponseBody { outputStream ->
                    inputStream.use { input ->
                        input.copyTo(outputStream)
                    }
                }
            }
    }

    fun getThumbnail(
        videoId: UUID,
        size: ThumbnailSize,
        type: ThumbnailType,
    ): Either<Failure, InputStream> {
        val metadata = metadataRepository
            .findById(videoId)
            .getOrElse { return MetadataFailure.NotFound.left() }

        val thumbnailSpec = ThumbnailSpecification
            .builder()
            .metadata(metadata)
            .size(size)
            .type(type)
            .build()
        val thumbnail = thumbnailRepository
            .findOne(thumbnailSpec)
            .getOrElse { return ThumbnailFailure.NotFound.left() }

        val path = thumbnail.path

        return minioService.getFileStream(path)
    }

    private fun fileAlreadyExists(
        fileName: FileName,
        userId: Long,
        course: String,
    ): Boolean {
        val specification = MetadataSpecification
            .builder()
            .fileName(fileName.value)
            .userId(userId)
            .courseName(course)
            .build()

        return metadataRepository.exists(specification)
    }

    private fun parseRange(rangeHeader: String, fileSize: Long): Pair<Long, Long> {
        // "bytes=0-1023" -> Pair(0, 1023)
        // "bytes=1024-" -> Pair(1024, fileSize-1)
        val range = rangeHeader.removePrefix("bytes=")
        val parts = range.split("-")
        val start = parts[0].toLongOrNull() ?: 0
        val end = if (parts[1].isBlank()) fileSize - 1 else parts[1].toLong()
        return Pair(start, end)
    }

    private fun copyLimitedBytes(
        inputStream: InputStream,
        outputStream: OutputStream,
        maxBytes: Long,
    ) {
        val buffer = ByteArray(8192) // 8KB buffer
        var totalCopied = 0L

        while (totalCopied < maxBytes) {
            val bytesToRead = minOf(buffer.size.toLong(), maxBytes - totalCopied).toInt()
            val bytesRead = inputStream.read(buffer, 0, bytesToRead)

            if (bytesRead == -1) break // EOF

            outputStream.write(buffer, 0, bytesRead)
            totalCopied += bytesRead
        }
    }
}