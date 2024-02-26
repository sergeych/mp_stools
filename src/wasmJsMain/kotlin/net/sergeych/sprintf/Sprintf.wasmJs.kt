package net.sergeych.sprintf

import kotlinx.datetime.Instant
actual fun ConvertToInstant(t: Any): Instant {
    throw IllegalArgumentException("Can't convert to LocalDateTime: $t")
}
