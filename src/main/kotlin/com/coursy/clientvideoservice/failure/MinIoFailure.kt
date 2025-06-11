package com.coursy.clientvideoservice.failure

class MinIoFailure(val exception: String?) : Failure {
    override fun message() = "Unexpected MinIo exception: $exception"
}
