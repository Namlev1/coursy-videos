package com.coursy.videos.dto

import com.coursy.videos.model.Metadata
import com.coursy.videos.types.FileName
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(description = "Response containing comprehensive video metadata information")
data class MetadataResponse(
    @Schema(
        description = "Unique identifier for the video metadata record",
        example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
        required = true
    )
    val id: UUID,

    @Schema(
        description = "Title of the video",
        example = "Introduction to Spring Boot",
        required = true,
    )
    val title: FileName,

    @Schema(
        description = "File system path where the video is stored",
        example = "/videos/user123/spring-boot-fundamentals/intro-video.mp4",
        required = true
    )
    val path: String,

    @Schema(
        description = "Name of the course this video belongs to",
        example = "Spring Boot Fundamentals",
        required = true,
    )
    val course: String,

    @Schema(
        description = "Unique identifier of the user who uploaded the video",
        example = "456",
        required = true,
    )
    val userId: Long,

    @Schema(
        description = "Size of the video file in bytes",
        example = "104857600",
        required = true,
    )
    val fileSize: Long,

    @Schema(
        description = "Timestamp when the video was uploaded to the system",
        example = "2024-03-15T14:30:00",
        required = true,
        type = "string",
        format = "date-time"
    )
    val uploadedAt: LocalDateTime,

    @Schema(
        description = "Duration of the video in seconds",
        example = "300",
    )
    val duration: Int = 0,
)

fun Metadata.toResponse() = MetadataResponse(
    id = this.id ?: UUID.randomUUID(),
    title = this.title,
    path = this.path,
    course = this.course,
    userId = this.userId,
    fileSize = this.fileSize,
    uploadedAt = this.uploadedAt,
    duration = this.duration ?: 0
)