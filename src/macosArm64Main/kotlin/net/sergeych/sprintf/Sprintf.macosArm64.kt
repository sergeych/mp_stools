package net.sergeych.sprintf

import kotlinx.datetime.Instant

actual fun ConvertToInstant(t: Any): Instant {
    throw IllegalArgumentException("can't convert to time instant: $t")
}