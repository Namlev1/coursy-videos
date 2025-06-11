package com.coursy.clientvideoservice.dto

import org.springframework.web.multipart.MultipartFile

data class VideoUploadRequest(
    val file: MultipartFile,
    val userId: Long,
    val courseName: String
)
