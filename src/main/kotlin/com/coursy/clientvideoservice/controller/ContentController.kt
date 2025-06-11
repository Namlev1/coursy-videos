package com.coursy.clientvideoservice.controller

import com.coursy.clientvideoservice.dto.VideoUploadRequest
import com.coursy.clientvideoservice.dto.VideoUploadResponse
import com.coursy.clientvideoservice.service.VideoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/videos")
class ContentController(
    private val videoService: VideoService,
) {

    @PostMapping("/upload")
    fun uploadVideo(
        @ModelAttribute request: VideoUploadRequest,
    ): ResponseEntity<Any> {
        return videoService
            .saveVideo(
                request.file,
                request.userId,
                request.courseName
            )
            .fold(
                { failure -> ResponseEntity.badRequest().body(failure.message()) },
                { path ->
                    ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(VideoUploadResponse(path))
                }
            )

    }
}