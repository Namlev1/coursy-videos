package com.coursy.videos.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.multipart.MultipartFile

@Schema(description = "Request for uploading a video file")
data class VideoUploadRequest(
    val file: MultipartFile,
    val userId: Long,
    val courseName: String,
    val title: String,
    val description: String,
)