package com.coursy.videos.repository

import com.coursy.videos.model.Content
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContentRepository : JpaRepository<Content, UUID> {
    fun findByCourse(courseId: UUID): List<Content>
}
