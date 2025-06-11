package com.coursy.clientvideoservice.controller

import com.coursy.clientvideoservice.service.VideoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/videos")
class ContentController(
    private val videoService: VideoService
) {

    @PostMapping("/upload")
    fun uploadVideo(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        return videoService
            .saveVideo(file)
            .fold(
                { failure -> ResponseEntity.badRequest().body(failure.message()) },
                { ResponseEntity.status(HttpStatus.CREATED).build() }
            )

    }
}