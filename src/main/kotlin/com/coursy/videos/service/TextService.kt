package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.dto.TextDto
import com.coursy.videos.dto.toDto
import com.coursy.videos.dto.toEntity
import com.coursy.videos.failure.TextFailure
import com.coursy.videos.model.Content
import com.coursy.videos.model.MaterialType
import com.coursy.videos.repository.ContentRepository
import com.coursy.videos.repository.TextRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TextService(
    private val textRepository: TextRepository,
    private val contentRepository: ContentRepository,
) {
    fun saveText(dto: TextDto): TextDto {
        if (dto.course == null) {
            throw IllegalArgumentException("Quiz request must contain course ID")
        }

        val courseContent = contentRepository
            .findByCourse(dto.course)
            .mapNotNull { content ->
                when {
                    content.metadata != null -> content.metadata
                    content.quiz != null -> content.quiz
                    else -> null
                }
            }
            .sortedWith { o1, o2 -> o1.position.compareTo(o2.position) }

        val textEntity = dto.toEntity()
        val position = if (courseContent.isEmpty()) 0 else (courseContent.last().position + 1)
        textEntity.position = position
        val savedText = textRepository.save(textEntity)

        val content = Content(
            course = dto.course,
            quiz = null,
            type = MaterialType.TEXT,
            metadata = null,
            text = savedText
        )
        contentRepository.save(content)

        return savedText.toDto()
    }

    fun getTextById(id: UUID): Either<TextFailure, TextDto> {
        val text = textRepository.findByIdOrNull(id)
            ?: return TextFailure.NotFound(id).left()
        return text.toDto().right()
    }
}