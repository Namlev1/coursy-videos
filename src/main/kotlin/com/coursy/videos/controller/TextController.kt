package com.coursy.videos.controller

import com.coursy.videos.dto.TextDto
import com.coursy.videos.service.TextService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/content/texts")
class TextController(
    private val textService: TextService
) {
    @GetMapping("/{id}")
    fun getTextById(@PathVariable id: UUID): ResponseEntity<Any> {
        return textService.getTextById(id)
            .fold(
                { ResponseEntity.status(HttpStatus.NOT_FOUND).body(it.message()) },
                { ResponseEntity.ok(it) }
            )
    }

    @PostMapping
    fun createNewText(@RequestBody textDto: TextDto): ResponseEntity<Any> {
        return ResponseEntity.ok(textService.saveText(textDto))
    }
}