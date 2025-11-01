package com.coursy.videos.service

import com.coursy.videos.dto.toDto
import com.coursy.videos.repository.ContentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class ContentService(
    private val contentRepository: ContentRepository
) {
    fun getCourseContent(courseId: UUID) =
        contentRepository.findByCourse(courseId)
            .map { it.toDto() }
}