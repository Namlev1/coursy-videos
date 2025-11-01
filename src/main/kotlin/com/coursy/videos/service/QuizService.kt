package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.dto.QuizDto
import com.coursy.videos.dto.toDto
import com.coursy.videos.dto.toEntity
import com.coursy.videos.failure.QuizFailure
import com.coursy.videos.model.Content
import com.coursy.videos.model.MaterialType
import com.coursy.videos.repository.ContentRepository
import com.coursy.videos.repository.QuizRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class QuizService(
    private val quizRepository: QuizRepository,
    private val contentRepository: ContentRepository
) {

    fun getQuizById(id: UUID): Either<QuizFailure, QuizDto> {
        val quiz = quizRepository.findByIdWithQuestions(id)
            ?: return QuizFailure.NotFound(id).left()
        return quiz.toDto().right()
    }

    @Transactional
    fun createQuiz(quizDto: QuizDto): QuizDto {
        if (quizDto.course == null) {
            throw IllegalArgumentException("Quiz request must contain course ID")
        }

        val courseContent = contentRepository
            .findByCourse(quizDto.course)
            .mapNotNull { content ->
                when {
                    content.metadata != null -> content.metadata
                    content.quiz != null -> content.quiz
                    else -> null
                }
            }
            .sortedWith { o1, o2 -> o1.position.compareTo(o2.position) }

        val quiz = quizDto.toEntity()
        val position = if (courseContent.isEmpty()) 0 else (courseContent.last().position + 1)
        quiz.position = position
        val savedQuiz = quizRepository.save(quiz)

        val content = Content(
            course = quizDto.course,
            quiz = savedQuiz,
            type = MaterialType.QUIZ,
            metadata = null,
            text = null
        )
        contentRepository.save(content)
        return savedQuiz.toDto()
    }

    @Transactional
    fun deleteQuiz(id: UUID): Either<QuizFailure, Unit> {
        if (!quizRepository.existsById(id)) {
            return QuizFailure.NotFound(id).left()
        }
        return quizRepository.deleteById(id).right()
    }
}