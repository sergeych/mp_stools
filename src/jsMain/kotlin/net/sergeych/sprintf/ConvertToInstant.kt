package net.sergeych.sprintf

import kotlin.time.Instant
import kotlin.js.Date

actual fun ConvertToInstant(t: Any): Instant = when(t) {
        is Date -> Instant.fromEpochMilliseconds(t.getTime().toLong())
        else -> throw IllegalArgumentException("Can't convert to LocalDateTime: $t")
    }
