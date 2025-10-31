package com.coursy.videos.failure

import java.util.*

sealed class QuizFailure : Failure {
    data class NotFound(val id: UUID) : QuizFailure()

    override fun message(): String = when (this) {
        is NotFound -> "Quiz with id=${id} was not found"
    }
}
