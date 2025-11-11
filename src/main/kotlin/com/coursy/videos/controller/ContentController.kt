package com.coursy.videos.controller

import com.coursy.videos.service.ContentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @DeleteMapping("/{contentId}")
    fun deleteContent(@PathVariable contentId: UUID): ResponseEntity<Any> {
        return contentService
            .deleteContent(contentId)
            .fold(
                { failure -> ResponseEntity.badRequest().body(failure.message()) },
                { ResponseEntity.noContent().build() }
            )
    }

}