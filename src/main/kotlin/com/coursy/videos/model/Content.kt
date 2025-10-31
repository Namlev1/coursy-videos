package com.coursy.videos.model

import jakarta.persistence.*
import java.util.*

@Entity
class Content(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    val type: MaterialType,

    @OneToOne
    @JoinColumn(name = "metadata_id")
    val metadata: Metadata?,

    @OneToOne
    @JoinColumn(name = "question_id")
    val question: Question?,
)