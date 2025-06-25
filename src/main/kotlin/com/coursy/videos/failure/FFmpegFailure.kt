package com.coursy.videos.failure

class FFmpegFailure(
    val exitCode: Int
) : Failure {
    override fun message() = "FFmpeg failed with exit code $exitCode"
}
