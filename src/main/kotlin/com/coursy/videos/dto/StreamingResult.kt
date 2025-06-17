package com.coursy.videos.dto

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

data class StreamingResult(
    val streamingBody: StreamingResponseBody,
    val start: Long,
    val end: Long,
    val fileSize: Long,
)