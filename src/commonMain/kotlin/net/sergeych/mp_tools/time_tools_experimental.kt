package net.sergeych.mptools

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

val Instant.isInPast: Boolean get() =
    this < Clock.System.now()

val Instant.isInFuture: Boolean get() =
    this > Clock.System.now()