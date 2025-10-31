package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.dto.QuizDto
import com.coursy.videos.dto.toDto
import com.coursy.videos.dto.toEntity
import com.coursy.videos.failure.QuizFailure
import com.coursy.videos.repository.QuizRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class QuizService(
    private val quizRepository: QuizRepository
) {

    fun getQuizById(id: UUID): Either<QuizFailure, QuizDto> {
        val quiz = quizRepository.findByIdWithQuestions(id)
            ?: return QuizFailure.NotFound(id).left()
        return quiz.toDto().right()
    }

    @Transactional
    fun createQuiz(quizDto: QuizDto): QuizDto {
        val quiz = quizDto.toEntity()
        val savedQuiz = quizRepository.save(quiz)
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