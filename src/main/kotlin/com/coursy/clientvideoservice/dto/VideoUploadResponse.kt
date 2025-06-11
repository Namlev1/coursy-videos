package com.coursy.clientvideoservice.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing the uploaded video URI")
data class VideoUploadResponse(
    @Schema(
        description = "URI/path to the uploaded video file",
        example = "user123/spring-boot-fundamentals/intro-video.mp4"
    )
    val uri: String,
)