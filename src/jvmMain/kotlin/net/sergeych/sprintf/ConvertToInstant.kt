package net.sergeych.sprintf.net.sergeych.mp_logger

import kotlinx.datetime.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

actual fun ConvertToInstant(t: Any): Instant =
    when (t) {
        is java.time.LocalDateTime -> Instant.fromEpochSeconds(
            t.atZone(ZoneId.systemDefault()).toEpochSecond()
        )
        is ZonedDateTime -> Instant.fromEpochSeconds(t.toEpochSecond())
        is java.time.Instant -> Instant.fromEpochMilliseconds(t.toEpochMilli())
        is Date -> Instant.fromEpochMilliseconds(t.time)
        else -> throw IllegalArgumentException("Can't convert to LocalDateTime: $t")
    }
