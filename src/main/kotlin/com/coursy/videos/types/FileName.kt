package com.coursy.videos.types

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.FileFailure
import org.springframework.web.multipart.MultipartFile

@JvmInline
value class FileName private constructor(val value: String) {
    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 50
        private val INVALID_CHARS = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        private val ALLOWED_EXTENSIONS = setOf("mp4")

        fun fromFile(file: MultipartFile): Either<FileFailure, FileName> {
            val originalName = file.originalFilename?.takeIf { it.isNotBlank() }
                ?: return FileFailure.NoName.left()

            return originalName
                .validateLength()
                .flatMap { it.validateExtension() }
                .flatMap { it.validateCharacters() }
                .map(::FileName)
        }

        private fun String.validateLength(): Either<FileFailure, String> = when {
            length < MIN_LENGTH -> FileFailure.NameTooShort(MIN_LENGTH).left()
            length > MAX_LENGTH -> FileFailure.NameTooLong(MAX_LENGTH).left()
            else -> right()
        }

        private fun String.validateExtension(): Either<FileFailure, String> {
            val extension = substringAfterLast('.', "")
            return when {
                extension.isEmpty() -> FileFailure.NoExtension.left()
                extension.lowercase() !in ALLOWED_EXTENSIONS -> FileFailure.InvalidExtension.left()
                else -> right()
            }
        }

        private fun String.validateCharacters(): Either<FileFailure, String> =
            if (any { it in INVALID_CHARS }) {
                FileFailure.InvalidCharacters.left()
            } else {
                right()
            }
    }
}