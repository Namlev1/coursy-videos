package com.coursy.videos.types

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.FileFailure
import org.springframework.web.multipart.MultipartFile

@JvmInline
value class ContentType private constructor(val value: String) {
    companion object {
        private val ALLOWED_TYPES = setOf("video/mp4")

        fun fromFile(file: MultipartFile): Either<FileFailure, ContentType> {
            val originalName = file.contentType?.takeIf { it.isNotBlank() }
                ?: return FileFailure.NoContentType.left()

            return originalName
                .validateAllowedType()
                .map(::ContentType)
        }

        private fun String.validateAllowedType(): Either<FileFailure, String> =
            if (this !in ALLOWED_TYPES) {
                FileFailure.InvalidContentType.left()
            } else {
                right()
            }
    }
}
