package com.coursy.clientvideoservice.controller

import com.coursy.clientvideoservice.service.MinIOService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/videos")
class ContentController(
    private val minioService: MinIOService
) {

    @PostMapping("/upload")
    fun uploadVideo(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<VideoUploadResponse> {

        if (file.isEmpty) {
            return ResponseEntity.badRequest().build()
        }

        val fileName = file.originalFilename ?: "video"

        return try {
            val fileUrl = minioService.uploadFile(
                fileName = fileName,
                inputStream = file.inputStream,
                contentType = file.contentType ?: "video/mp4",
                size = file.size
            )

            ResponseEntity.ok(VideoUploadResponse(fileName, fileUrl))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
}

data class VideoUploadResponse(
    val fileName: String,
    val fileUrl: String
)