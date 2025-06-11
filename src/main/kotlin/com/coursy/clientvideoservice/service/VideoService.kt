package com.coursy.clientvideoservice.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import com.coursy.clientvideoservice.failure.Failure
import com.coursy.clientvideoservice.failure.FileFailure
import com.coursy.clientvideoservice.types.ContentType
import com.coursy.clientvideoservice.types.FileName
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class VideoService(
    private val minioService: MinIOService
) {
    fun saveVideo(file: MultipartFile): Either<Failure, Any> {
        if (file.isEmpty) {
            return FileFailure.Empty.left()
        }
        val fileName = FileName.fromFile(file).getOrElse { return it.left() }
        val contentType = ContentType.fromFile(file).getOrElse { return it.left() }

        return minioService.uploadFile(
            fileName = fileName.value,
            inputStream = file.inputStream,
            contentType = contentType.value,
            size = file.size
        )
    }
}