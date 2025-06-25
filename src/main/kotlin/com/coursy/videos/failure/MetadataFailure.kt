package com.coursy.videos.failure

sealed class MetadataFailure : Failure {
    data object NotFound : MetadataFailure()

    override fun message(): String = when (this) {
        is NotFound -> "File not found"
    }
}
