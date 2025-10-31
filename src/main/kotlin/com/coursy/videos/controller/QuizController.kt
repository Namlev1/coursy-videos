package com.coursy.videos.controller

import com.coursy.videos.dto.QuizDto
import com.coursy.videos.service.QuizService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/content/quiz")
class QuizController(
    private val quizService: QuizService
) {
    @GetMapping("/{id}")
    fun getQuizById(@PathVariable id: UUID): ResponseEntity<Any> {
        return quizService.getQuizById(id)
            .fold(
                { ResponseEntity.status(HttpStatus.NOT_FOUND).body(it.message()) },
                { ResponseEntity.ok(it) }
            )
    }

    @PostMapping
    fun postQuiz(@RequestBody quizDto: QuizDto): ResponseEntity<Any> {
        return ResponseEntity.ok(quizService.createQuiz(quizDto))
    }
}
