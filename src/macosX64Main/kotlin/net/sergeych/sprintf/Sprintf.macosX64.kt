package net.sergeych.sprintf

import kotlinx.datetime.Instant

actual fun ConvertToInstant(t: Any): Instant {
    // kotlin native date types... have no idea what these are
    // pls add your code here and create PR from it
    throw IllegalArgumentException("can't convert to time instant: $t")
}