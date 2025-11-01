package com.coursy.videos.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "text_material")
class Text(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    override var position: Int
) : Ordered