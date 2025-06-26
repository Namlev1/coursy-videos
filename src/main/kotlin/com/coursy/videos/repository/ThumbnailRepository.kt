package com.coursy.videos.repository

import com.coursy.videos.model.Thumbnail
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ThumbnailRepository : JpaRepository<Thumbnail, UUID>