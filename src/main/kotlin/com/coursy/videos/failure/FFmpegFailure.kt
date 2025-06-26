package com.coursy.videos.failure

sealed class FFmpegFailure(
) : Failure {
    object DurationParsingError : FFmpegFailure()
    data class ProcessingError(val exitCode: Int) : FFmpegFailure()

    override fun message(): String = when (this) {
        is DurationParsingError -> "Could not parse duration"
        is ProcessingError -> "FFmpeg failed with exit code $exitCode"
    }
}
