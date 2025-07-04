package com.coursy.videos.failure

sealed class ThumbnailFailure : Failure {
    data object NotFound : ThumbnailFailure()

    override fun message(): String = when (this) {
        is NotFound -> "Thumbnail not found"
    }
}
