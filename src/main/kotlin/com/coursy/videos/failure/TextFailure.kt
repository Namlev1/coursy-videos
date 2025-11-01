package com.coursy.videos.failure

import java.util.*

sealed class TextFailure : Failure {
    data class NotFound(val id: UUID) : TextFailure()

    override fun message(): String = when (this) {
        is NotFound -> "Quiz with id=${id} was not found"
    }
}
