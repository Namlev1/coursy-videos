package com.coursy.videos.controller

import com.coursy.videos.service.ContentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/content")
class ContentController(
    private val contentService: ContentService
) {

    @GetMapping("/course/{courseId}")
    fun getCourseContent(@PathVariable courseId: UUID): ResponseEntity<Any> {
        return ResponseEntity
            .ok()
            .body(contentService.getCourseContent(courseId))
    }

}