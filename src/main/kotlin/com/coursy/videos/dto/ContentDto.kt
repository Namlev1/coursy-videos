package com.coursy.videos.dto

import com.coursy.videos.model.Content
import com.coursy.videos.model.MaterialType
import java.util.*

data class ContentDto(
    val id: UUID,
    val title: String,
    val videoDuration: Double?,
    val type: MaterialType,
    val position: Int,
    val quizId: UUID?,
    val videoId: UUID?,
    val textId: UUID?
)

fun Content.toDto(): ContentDto {
    return when (type) {
        MaterialType.VIDEO -> ContentDto(
            id = id,
            title = metadata?.title ?: "Untitled",
            videoDuration = metadata?.duration,
            type = type,
            position = metadata?.position ?: 0,
            quizId = null,
            videoId = metadata?.id,
            textId = null
        )

        MaterialType.QUIZ -> ContentDto(
            id = id,
            title = quiz?.title ?: "Untitled",
            videoDuration = null,
            type = type,
            position = quiz?.position ?: 0,
            quizId = quiz?.id,
            videoId = null,
            textId = null
        )

        MaterialType.TEXT -> ContentDto(
            id = id,
            title = text?.title ?: "Untitled",
            videoDuration = null,
            type = type,
            position = text?.position ?: 0,
            quizId = null,
            videoId = null,
            textId = text?.id
        )

        else -> ContentDto(
            id = id,
            title = "Untitled",
            videoDuration = null,
            type = type,
            position = 0,
            quizId = null,
            videoId = null,
            textId = null
        )
    }
}