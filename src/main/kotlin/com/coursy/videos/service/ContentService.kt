package com.coursy.videos.service

import com.coursy.videos.dto.toDto
import com.coursy.videos.repository.ContentRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ContentService(
    private val contentRepository: ContentRepository
) {
    fun getCourseContent(courseId: UUID) =
        contentRepository.findByCourse(courseId)
            .map { it.toDto() }
}