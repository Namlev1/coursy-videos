package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.dto.toDto
import com.coursy.videos.failure.ContentFailure
import com.coursy.videos.failure.Failure
import com.coursy.videos.model.*
import com.coursy.videos.repository.ContentRepository
import com.coursy.videos.repository.MetadataRepository
import com.coursy.videos.repository.QuizRepository
import com.coursy.videos.repository.TextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class ContentService(
    private val contentRepository: ContentRepository,
    private val textRepository: TextRepository,
    private val quizRepository: QuizRepository,
    private val metadataRepository: MetadataRepository,
    private val videoService: VideoService
) {
    fun getCourseContent(courseId: UUID) =
        contentRepository.findByCourse(courseId)
            .map { it.toDto() }

    @Transactional
    fun deleteContent(contentId: UUID): Either<Failure, Unit> {
        val content = contentRepository.findById(contentId)
            .orElse(null) ?: return ContentFailure.NotFound(contentId).left()

        val contentItem = when (content.type) {
            MaterialType.TEXT -> content.text!!
            MaterialType.QUIZ -> content.quiz!!
            MaterialType.VIDEO -> content.metadata!!
        }
        val courseId = content.course
        val allContent: List<Ordered> = contentRepository
            .findByCourse(courseId)
            .map { item ->
                when (item.type) {
                    MaterialType.TEXT -> item.text!!
                    MaterialType.QUIZ -> item.quiz!!
                    MaterialType.VIDEO -> item.metadata!!
                }
            }

        allContent
            .filter { it.position > contentItem.position }
            .forEach { itemToUpdate ->
                itemToUpdate.position -= 1
                when (itemToUpdate) {
                    is Text -> textRepository.save(itemToUpdate)
                    is Quiz -> quizRepository.save(itemToUpdate)
                    is Metadata -> metadataRepository.save(itemToUpdate)
                }
            }
        when (content.type) {
            MaterialType.TEXT -> textRepository.delete(contentItem as Text)
            MaterialType.QUIZ -> quizRepository.delete(contentItem as Quiz)
            MaterialType.VIDEO -> videoService.deleteVideo(contentItem as Metadata)
        }
        contentRepository.delete(content)

        return Unit.right()
    }
}