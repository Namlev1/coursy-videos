package com.coursy.videos.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "question")
class Question(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    val quiz: Quiz,

    @Column(nullable = false, length = 2000)
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var answerSelectionType: AnswerSelectionType,

    @Column(nullable = false)
    var points: Int,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val answers: MutableList<Answer> = mutableListOf(),

    @Column(length = 2000)
    var explanation: String? = null,

    @Column(nullable = false)
    var orderIndex: Int
) {
    fun addAnswer(answer: Answer) {
        answers.add(answer)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Question) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Question(id=$id, content='${content.take(50)}...', orderIndex=$orderIndex)"
    }
}