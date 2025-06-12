package com.coursy.clientvideoservice.failure

sealed class FileFailure : Failure {
    data object Empty : FileFailure()
    data object NoName : FileFailure()
    data class NameTooShort(val minLength: Int) : FileFailure()
    data class NameTooLong(val maxLength: Int) : FileFailure()
    data object InvalidContentType : FileFailure()
    data object NoExtension : FileFailure()
    data object InvalidExtension : FileFailure()
    data object InvalidCharacters : FileFailure()
    data object NoContentType : FileFailure()
    data object AlreadyExists : FileFailure()
    data object InvalidId : FileFailure()

    override fun message(): String = when (this) {
        is Empty -> "File is empty."
        is NoName -> "File has no name."
        is NameTooLong -> "File name is too long (maximum length: $maxLength)."
        is NameTooShort -> "File name is too short (minimum length: $minLength)."
        is InvalidContentType -> "Only video/mp4 file type is supported."
        is NoExtension -> "File contains no extension."
        is InvalidExtension -> "Only .mp4 video format is supported."
        is InvalidCharacters -> "Filename contains invalid characters."
        is NoContentType -> "File contains no content type."
        is AlreadyExists -> "File of this name already exists in this course"
        is InvalidId -> "File with given ID does not exist"
    }
}
