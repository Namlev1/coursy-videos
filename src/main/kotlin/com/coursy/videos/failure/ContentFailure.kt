package com.coursy.videos.failure

import java.util.*

sealed class ContentFailure : Failure {
    data class NotFound(val id: UUID) : ContentFailure()

    override fun message(): String = when (this) {
        is NotFound -> "Content with id=${id} was not found"
    }
}
