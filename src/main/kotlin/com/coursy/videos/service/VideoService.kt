package com.coursy.videos.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.videos.dto.MetadataResponse
import com.coursy.videos.dto.toResponse
import com.coursy.videos.failure.Failure
import com.coursy.videos.failure.FileFailure
import com.coursy.videos.model.Metadata
import com.coursy.videos.repository.MetadataRepository
import com.coursy.videos.repository.MetadataSpecification
import com.coursy.videos.types.ContentType
import com.coursy.videos.types.FileName
import com.coursy.videos.utils.toEither
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
@Transactional
class VideoService(
    private val minioService: MinIOService,
    private val metadataRepository: MetadataRepository,
    private val pagedResourcesAssembler: PagedResourcesAssembler<MetadataResponse>,
) {
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
        val path = "$userId/$course/${fileName.value}"

        if (fileAlreadyExists(fileName, userId, course)) {
            return FileFailure.AlreadyExists.left()
        }

        // Save metadata first
        var metadata = Metadata(
            title = fileName.value,
            path = path,
            course = course,
            userId = userId,
            fileSize = file.size,
        )
        metadata = metadataRepository.save(metadata)

        // Save file in MinIO
        return minioService.uploadFile(
            path = path,
            inputStream = file.inputStream,
            contentType = contentType.value,
            size = file.size
        ).map { metadata.toResponse() }
    }

    fun downloadVideo(
        fileName: String,
        userId: Long,
        course: String,
    ): Either<Failure, InputStream> {
        // TODO authorization: users can only download its videos, ADMIN can download any.
        val fileName = FileName.fromString(fileName).getOrElse { return it.left() }
        val path = "$userId/$course/${fileName.value}"

        if (!fileAlreadyExists(fileName, userId, course)) {
            return FileFailure.NotFound.left()
        }

        return minioService.downloadFile(path)
    }

    fun getVideo(videoId: Long): Either<FileFailure, MetadataResponse> =
        metadataRepository.findById(videoId)
            .toEither(
                { FileFailure.InvalidId.left() },
                { it.toResponse().right() }
            )

    fun getPage(pageRequest: PageRequest) =
        metadataRepository.findAll(pageRequest)
            .map { it.toResponse() }
            .let { pagedResourcesAssembler.toModel(it) }

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
}