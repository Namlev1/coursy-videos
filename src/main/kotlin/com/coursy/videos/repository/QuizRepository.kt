package com.coursy.videos.repository

import com.coursy.videos.model.Quiz
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface QuizRepository : JpaRepository<Quiz, UUID> {
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    fun findByIdWithQuestions(id: UUID): Quiz?
}