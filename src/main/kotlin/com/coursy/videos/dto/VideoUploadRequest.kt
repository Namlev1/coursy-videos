package com.coursy.videos.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Schema(description = "Request for uploading a video file")
data class VideoUploadRequest(
    val file: MultipartFile,
    val course: UUID,
    val title: String,
    val description: String,
    val position: Int
)