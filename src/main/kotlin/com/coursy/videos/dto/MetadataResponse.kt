package com.coursy.videos.dto

import com.coursy.videos.model.Metadata
import com.coursy.videos.types.FileName
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(description = "Response containing comprehensive video metadata information")
data class MetadataResponse(
    val id: UUID,

    val fileName: FileName,

    val path: String,

    val course: UUID,

    val fileSize: Long,

    val uploadedAt: LocalDateTime,

    val duration: Double = 0.0,

    val title: String,

    val description: String,
)

fun Metadata.toResponse() = MetadataResponse(
    id = this.id,
    fileName = this.fileName,
    path = this.path,
    course = this.course,
    fileSize = this.fileSize,
    uploadedAt = this.uploadedAt,
    duration = this.duration,
    title = this.title,
    description = this.description,
)