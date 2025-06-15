package com.coursy.videos.failure

class MinIoFailure(val exception: String?) : Failure {
    override fun message() = "Unexpected MinIo exception: $exception"
}
