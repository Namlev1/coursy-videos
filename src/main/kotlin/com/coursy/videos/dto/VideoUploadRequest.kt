package com.coursy.videos.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.multipart.MultipartFile

@Schema(description = "Request for uploading a video file")
data class VideoUploadRequest(
    @Schema(
        description = "Video file to upload",
        required = true,
        type = "string",
        format = "binary"
    )
    val file: MultipartFile,

    @Schema(
        description = "ID of the user uploading the video",
        required = true,
        example = "123"
    )
    val userId: Long,

    @Schema(
        description = "Name of the course this video belongs to",
        required = true,
        example = "Spring Boot Fundamentals"
    )
    val courseName: String,
)