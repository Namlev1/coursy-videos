package com.coursy.videos.failure

class RangeFailure(val start: Long, val end: Long) : Failure {
    override fun message() = "Invalid range: [$start, $end]"
}