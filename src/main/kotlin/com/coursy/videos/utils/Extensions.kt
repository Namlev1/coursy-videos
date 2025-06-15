package com.coursy.videos.utils

import java.util.*

fun <T, R> Optional<T>.toEither(
    ifEmpty: () -> R,
    ifPresent: (T) -> R,
): R = if (isPresent) ifPresent(get()) else ifEmpty()
