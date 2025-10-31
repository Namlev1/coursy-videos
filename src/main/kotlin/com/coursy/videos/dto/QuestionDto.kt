package com.coursy.videos.dto

import com.coursy.videos.model.Answer
import com.coursy.videos.model.AnswerSelectionType
import com.coursy.videos.model.Question
import com.coursy.videos.model.Quiz

data class QuestionDto(
    val question: String,
    val questionType: String = "text",
    val answerSelectionType: String,
    val answers: List<String>,
    val correctAnswer: Any,
    val messageForCorrectAnswer: String = "Correct answer. Good job.",
    val messageForIncorrectAnswer: String = "Incorrect answer. Please try again.",
    val explanation: String? = null,
    val point: String = "10"
)

fun Question.toDto(): QuestionDto {
    val sortedAnswers = this.answers.sortedBy { it.orderIndex }

    return QuestionDto(
        question = this.content,
        answerSelectionType = when (this.answerSelectionType) {
            AnswerSelectionType.SINGLE -> "single"
            AnswerSelectionType.MULTIPLE -> "multiple"
        },
        answers = sortedAnswers.map { it.content },
        correctAnswer = when (this.answerSelectionType) {
            AnswerSelectionType.SINGLE -> {
                val correctIndex = sortedAnswers.indexOfFirst { it.isCorrect }
                (correctIndex + 1).toString()
            }

            AnswerSelectionType.MULTIPLE -> {
                sortedAnswers
                    .mapIndexedNotNull { index, answer ->
                        if (answer.isCorrect) index + 1 else null
                    }
            }
        },
        explanation = this.explanation,
        point = this.points.toString()
    )
}

fun QuestionDto.toEntity(quiz: Quiz, orderIndex: Int): Question {
    val question = Question(
        quiz = quiz,
        content = this.question,
        answerSelectionType = when (this.answerSelectionType.lowercase()) {
            "single" -> AnswerSelectionType.SINGLE
            "multiple" -> AnswerSelectionType.MULTIPLE
            else -> throw IllegalArgumentException("Invalid answerSelectionType: ${this.answerSelectionType}")
        },
        points = this.point.toIntOrNull() ?: 10,
        explanation = this.explanation,
        orderIndex = orderIndex
    )

    // Parsuj correctAnswer i stwórz odpowiedzi
    val correctIndices = when (val correct = this.correctAnswer) {
        is String -> setOf(correct.toInt())
        is Int -> setOf(correct)
        is List<*> -> correct.filterIsInstance<Int>().toSet()
        else -> throw IllegalArgumentException("Invalid correctAnswer format: $correct")
    }

    // Dodaj odpowiedzi z właściwą relacją
    this.answers.forEachIndexed { index, answerContent ->
        val isCorrect = correctIndices.contains(index + 1) // react-quiz-component używa indeksów od 1
        val answer = Answer(
            question = question,
            content = answerContent,
            isCorrect = isCorrect,
            orderIndex = index
        )
        question.addAnswer(answer)
    }

    return question
}