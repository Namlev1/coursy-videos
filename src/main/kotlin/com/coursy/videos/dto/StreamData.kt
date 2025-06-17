package com.coursy.videos.dto

import com.coursy.videos.types.FileName
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

data class StreamData(
    val streamingBody: StreamingResponseBody,
    val fileSize: Long,
    val fileName: FileName,
)
