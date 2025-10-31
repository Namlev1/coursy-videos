package com.coursy.videos.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "quiz")
class Quiz(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @Column(length = 1000)
    var synopsis: String?,

    @Column(nullable = false)
    var passingScore: Int,

    @OneToMany(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val questions: MutableList<Question> = mutableListOf(),

    @Column
    override var position: Int,
) : Ordered {
    fun addQuestion(question: Question) {
        questions.add(question)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Quiz) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Quiz(id=$id, title='$title')"
    }
}