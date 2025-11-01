package com.coursy.videos.dto

import com.coursy.videos.model.Text
import java.util.*

data class TextDto(
    val title: String,
    val position: Int,
    val course: UUID?,
    val id: UUID?,
    val content: String
)

fun Text.toDto(): TextDto {
    return TextDto(
        title = this.title,
        position = this.position,
        id = this.id,
        course = null,
        content = this.content
    )
}

fun TextDto.toEntity(): Text {
    return Text(
        title = this.title,
        position = this.position,
        content = this.content
    )
}