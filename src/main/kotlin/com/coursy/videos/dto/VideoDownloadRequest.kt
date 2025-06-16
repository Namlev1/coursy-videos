package com.coursy.videos.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for downloading a video file")
data class VideoDownloadRequest(
    @Schema(
        description = "Video filename to download",
        required = true,
        type = "string",
        format = "binary"
    )
    val fileName: String,

    // TODO authorization: users can only download its videos, ADMIN can download any.
    @Schema(
        description = "ID of the user possessing the video",
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
