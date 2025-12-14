package net.sergeych.mptools

import kotlin.time.Clock
import kotlin.time.Instant

val Instant.isInPast: Boolean get() =
    this < Clock.System.now()

val Instant.isInFuture: Boolean get() =
    this > Clock.System.now()

fun Now(): Instant {
    return Clock.System.now()
}