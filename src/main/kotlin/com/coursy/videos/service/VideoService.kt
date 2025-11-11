package com.coursy.videos.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.videos.dto.*
import com.coursy.videos.failure.*
import com.coursy.videos.model.*
import com.coursy.videos.repository.*
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
    private val contentRepository: ContentRepository,
    private val thumbnailRepository: ThumbnailRepository,
    private val pagedResourcesAssembler: PagedResourcesAssembler<MetadataResponse>,
    private val videoProcessingService: VideoProcessingService,
    private val videoQualityRepository: VideoQualityRepository
) {
    private val processingScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    fun saveVideo(
        request: VideoUploadRequest
    ): Either<Failure, MetadataResponse> {
        val file = request.file
        if (file.isEmpty) {
            return FileFailure.Empty.left()
        }

        val course = request.course
        val fileName = FileName.fromFile(file).getOrElse { return it.left() }
        val contentType = ContentType.fromFile(file).getOrElse { return it.left() }
        val id = UUID.randomUUID()
        val dir = "$course/$id"

        if (fileAlreadyExists(fileName, course)) {
            return FileFailure.AlreadyExists.left()
        }

        val courseContent = contentRepository
            .findByCourse(request.course)
            .mapNotNull { content ->
                when {
                    content.metadata != null -> content.metadata
                    content.quiz != null -> content.quiz
                    else -> null
                }
            }
            .sortedWith { o1, o2 -> o1.position.compareTo(o2.position) }
        val position = if (courseContent.isEmpty()) 0 else (courseContent.last().position + 1)

        // Save metadata first
        var metadata = Metadata(
            id = id,
            fileName = fileName,
            path = dir,
            course = course,
            fileSize = file.size,
            status = ProcessingStatus.UPLOADED,
            title = request.title,
            description = request.description,
            position = position
        )
        metadata = metadataRepository.save(metadata)

        val content = Content(
            course = request.course,
            quiz = null,
            type = MaterialType.VIDEO,
            metadata = metadata,
            text = null
        )
        contentRepository.save(content)

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
        val fileName = metadata.fileName

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
        val path = "${metadata.course}/${metadata.fileName}"

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

    fun getVideosByCourseId(courseId: UUID): List<MetadataResponse> {
        val specification = MetadataSpecification
            .builder()
            .courseName(courseId)
            .build()
        val videos = metadataRepository.findAll(specification)
        return videos.map { it.toResponse() }
    }

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
        type: ThumbnailType?,
    ): Either<Failure, InputStream> {
        val metadata = metadataRepository
            .findById(videoId)
            .getOrElse { return MetadataFailure.NotFound.left() }

        val findPrimary = type == null

        val thumbnail = metadata.thumbnails.find { thumbnail ->
            when {
                thumbnail.size != size -> false
                findPrimary -> thumbnail.primary == true
                thumbnail.type != type -> false
                else -> true
            }
        } ?: return ThumbnailFailure.NotFound.left()

        return minioService.getFileStream(thumbnail.path)
    }

    fun deleteVideo(metadata: Metadata) {
        videoQualityRepository.deleteByMetadataId(metadata.id)
        minioService.deleteFolder(metadata.path)
        metadataRepository.delete(metadata)
    }
    
    private fun fileAlreadyExists(
        fileName: FileName,
        course: UUID,
    ): Boolean {
        val specification = MetadataSpecification
            .builder()
            .fileName(fileName.value)
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