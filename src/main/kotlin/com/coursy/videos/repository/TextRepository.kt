package com.coursy.videos.repository

import com.coursy.videos.model.Text
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TextRepository : JpaRepository<Text, UUID>