package net.sergeych.sprintf

import kotlinx.datetime.Instant

actual fun ConvertToInstant(t: Any): Instant {
    // TODO: please add ios-specific conversions here
    throw IllegalArgumentException("can't convert to time instant: $t")
}