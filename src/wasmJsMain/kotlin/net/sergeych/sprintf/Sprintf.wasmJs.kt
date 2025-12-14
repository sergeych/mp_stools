package net.sergeych.sprintf

import kotlin.time.Instant
actual fun ConvertToInstant(t: Any): Instant {
    throw IllegalArgumentException("Can't convert to LocalDateTime: $t")
}
