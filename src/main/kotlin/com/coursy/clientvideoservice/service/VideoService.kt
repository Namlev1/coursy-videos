package com.coursy.clientvideoservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.clientvideoservice.failure.Failure
import com.coursy.clientvideoservice.failure.FileFailure
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
        val fileName = file.originalFilename ?: return FileFailure.NoName.left()
        
        val contentType = file.contentType
        if (contentType != "video/mp4") {
            return FileFailure.InvalidContentType.left()
        }

        return minioService.uploadFile(
            fileName = fileName,
            inputStream = file.inputStream,
            contentType = contentType,
            size = file.size
        )
    }
}