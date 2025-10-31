package com.coursy.videos.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "answers")
class Answer(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @Column(nullable = false, length = 1000)
    var content: String,

    @Column(nullable = false)
    var isCorrect: Boolean,

    @Column(nullable = false)
    var orderIndex: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Answer) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Answer(id=$id, content='${content.take(30)}...', isCorrect=$isCorrect)"
    }
}