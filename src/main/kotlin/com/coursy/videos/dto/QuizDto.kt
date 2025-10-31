package com.coursy.videos.dto

import com.coursy.videos.model.Quiz
import java.util.*

data class QuizDto(
    val quizTitle: String,
    val quizSynopsis: String?,
    val nrOfQuestions: String, // String, bo komponent tego oczekuje
    val questions: List<QuestionDto>,
    val position: Int,
    val course: UUID?,
    val id: UUID?
)

fun Quiz.toDto(): QuizDto {
    return QuizDto(
        quizTitle = this.title,
        quizSynopsis = this.synopsis,
        nrOfQuestions = this.questions.size.toString(),
        questions = this.questions.sortedBy { it.orderIndex }.map { it.toDto() },
        position = this.position,
        id = this.id,
        course = null
    )
}

fun QuizDto.toEntity(): Quiz {
    val quiz = Quiz(
        title = this.quizTitle,
        synopsis = this.quizSynopsis,
        passingScore = 70,
        position = this.position,
    )

    this.questions.forEachIndexed { index, questionDto ->
        val question = questionDto.toEntity(quiz, index)
        quiz.addQuestion(question)
    }

    return quiz
}